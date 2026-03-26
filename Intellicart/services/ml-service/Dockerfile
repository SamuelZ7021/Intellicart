FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# Instalamos auto-instrumentación
RUN opentelemetry-bootstrap -a install

# Aseguramos que el directorio para métricas multiproceso exista
RUN mkdir -p /tmp && chmod 777 /tmp

COPY . .

# Ejecutamos uvicorn con instrumentación y auto-reload para desarrollo
ENTRYPOINT ["opentelemetry-instrument", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]