import os
import shutil
from fastapi import FastAPI, UploadFile, File, HTTPException
from faster_whisper import WhisperModel

app = FastAPI(title="Interview Analyzer - STT Service")

# Загружаем модель при старте.
# Для M1/M2/M3 Mac ставим compute_type="float16" или "int8" (для экономии памяти)
# Модель "base" или "small" дает отличный баланс качества и скорости на русском языке.
print("Загрузка модели Whisper...")
model = WhisperModel("base", device="cpu", compute_type="int8")
print("Модель Whisper успешно загружена!")

@app.post("/transcribe")
async def transcribe_audio(file: UploadFile = File(...)):
    # Сохраняем временный файл
    temp_file_path = f"temp_{file.filename}"

    try:
        with open(temp_file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # Транскрибируем
        segments, info = model.transcribe(temp_file_path, language="ru")

        # Собираем распознанный текст и сегменты с таймкодами
        result_segments = []
        full_text = []

        for segment in segments:
            full_text.append(segment.text)
            result_segments.append({
                "start": round(segment.start, 2),
                "end": round(segment.end, 2),
                "text": segment.text.strip()
            })

        return {
            "language": info.language,
            "duration": round(info.duration, 2),
            "text": " ".join(full_text).strip(),
            "segments": result_segments
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        # Обязательно удаляем временный файл за собой
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)
