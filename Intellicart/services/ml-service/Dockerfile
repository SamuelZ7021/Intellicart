# services/ml-service/Dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
# Instalamos los componentes de auto-instrumentación
RUN opentelemetry-bootstrap -a install
COPY . .
# Ejecutamos con el wrapper de instrumentación
ENTRYPOINT ["opentelemetry-instrument", "python", "-m", "app.main"]