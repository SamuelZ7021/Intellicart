FROM python:3.11-slim

WORKDIR /app

# Install system dependencies if needed (e.g. for potential native extensions)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Expose ports for FastAPI (8000) and gRPC (50051)
EXPOSE 8000 50051

# Create a start script to run both servers (simulated for now or use supervisord if needed,
# but for simplicity we might just run the main entrypoint if it starts both)
# Assuming main.py starts everything or we need a specific command.
# Based on file list, main.py seems to be the entry point.
CMD ["python", "app/main.py"]