echo "컨테이너를 특정포터로 open"
docker run -d -p 8000:8000 --name my-userservice my-userservice

docker ps | findstr my-userservice