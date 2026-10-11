import os
import re
import shutil
import uuid
from fastapi import FastAPI, UploadFile, File, HTTPException
from faster_whisper import WhisperModel

app = FastAPI(title="Interview Analyzer - STT Service")

# Подсказка модели: самые частые термины Java-собеседований (до ~200 токенов)
INITIAL_PROMPT = (
    "Собеседование на Java-разработчика. Термины: Java, Kotlin, Spring Boot, "
    "Spring Batch, Spring Data, Spring Security, Spring Cloud, Hibernate, JPA, "
    "@Transactional, PostgreSQL, Liquibase, Apache Kafka, RabbitMQ, Redis, "
    "Kubernetes, Docker, Jenkins, DevOps, CI/CD, REST API, gRPC, endpoint, "
    "Circuit Breaker, idempotent consumer, retry, JUnit, Mockito, Gradle, Maven, "
    "Out of Memory, микросервисы, батчи."
)

# (регулярка, замена). Регистр не учитывается, падежные окончания ловит \w*
REPLACEMENTS = [
    (r"\bд[еэ][бп][оа]?п[сc]\w*", "DevOps"),
    (r"\bдж[еэ]нкин\w*", "Jenkins"),
    (r"\bка[вф]к\w*", "Kafka"),
    (r"\bкубер[нн]ет[еэи]с\w*", "Kubernetes"),
    (r"\bспринг[\s-]*(майс\w*|бальш\w*|бэтч|батч|бач)", "Spring Batch"),
    (r"\bспринг[\s-]+(бут|бот)\b", "Spring Boot"),
    (r"\bтранзакц?ионал\b", "@Transactional"),
    (r"\bхибер[нн]ейт\w*", "Hibernate"),
    (r"\bпостгр[еэ]с\w*", "PostgreSQL"),
    (r"\bдокер\w*", "Docker"),
    (r"\bредис\w*", "Redis"),
    (r"\bликви[бв]ейс\w*", "Liquibase"),
    (r"\bр[еэ]бит\s?эм\s?кью\b", "RabbitMQ"),
    (r"\bсерк[иу]т\s+брейкер\w*", "Circuit Breaker"),
    (r"\bджей\s?юнит\b", "JUnit"),
    (r"\bмоккито\b", "Mockito"),
    (r"\bджпа\b", "JPA"),
    (r"\bэндпоинт\w*", "endpoint"),
    (r"\bбочи\b", "батчи"),
    (r"\btime\s?out\b", "timeout"),
]
COMPILED = [(re.compile(p, re.IGNORECASE), r) for p, r in REPLACEMENTS]


def fix_terms(text: str) -> str:
    for pattern, replacement in COMPILED:
        text = pattern.sub(replacement, text)
    return text


print("Загрузка модели Whisper...")
model = WhisperModel("small", device="cpu", compute_type="int8")
print("Модель Whisper успешно загружена!")


@app.post("/transcribe")
def transcribe_audio(file: UploadFile = File(...)):
    temp_file_path = f"temp_{uuid.uuid4().hex}.wav"

    try:
        with open(temp_file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        segments, info = model.transcribe(
            temp_file_path,
            language="ru",
            beam_size=5,
            vad_filter=True,
            condition_on_previous_text=False,
            initial_prompt=INITIAL_PROMPT,
        )

        result_segments = []
        full_text = []
        for segment in segments:
            text = fix_terms(segment.text.strip())
            full_text.append(text)
            result_segments.append({
                "start": round(segment.start, 2),
                "end": round(segment.end, 2),
                "text": text,
            })

        return {
            "language": info.language,
            "duration": round(info.duration, 2),
            "text": " ".join(full_text).strip(),
            "segments": result_segments,
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)