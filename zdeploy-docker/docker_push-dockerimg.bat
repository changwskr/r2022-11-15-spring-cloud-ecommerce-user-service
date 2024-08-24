echo "컨테이너 이미지를 docker hub에 저장"
docker tag my-userservice changwskr/my-userservice:1.0
docker push changwskr/my-userservice:1.0