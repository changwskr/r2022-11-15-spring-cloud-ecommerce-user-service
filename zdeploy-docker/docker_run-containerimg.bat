echo "컨테이너를 특정포터로 open"
rem docker run -d -p 9090:9090 --name my-userservice my-userservice
docker run -d changwskr/my-userservice:1.0

docker ps | findstr my-userservice