#!/bin/bash
# Build & push the frontend image to Docker Hub. Run locally, from the frontend/ dir,
# after `docker login`. Usage: ./build-push-fe.sh [tag]   (default tag: latest)
set -euo pipefail

IMAGE="lephianhdev386ht/library-management-system-frontend"
TAG="${1:-latest}"

echo "Build & Push: $IMAGE:$TAG"

# --platform linux/amd64: máy local có thể khác kiến trúc với VPS (linux/amd64).
docker build --pull --platform linux/amd64 -t "$IMAGE:$TAG" .

if [ "$TAG" != "latest" ]; then
  docker tag "$IMAGE:$TAG" "$IMAGE:latest"
fi

docker push "$IMAGE:$TAG"

if [ "$TAG" != "latest" ]; then
  docker push "$IMAGE:latest"
fi

echo "Done: $IMAGE:$TAG"
