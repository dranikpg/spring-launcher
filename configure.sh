cat << EOF
Setting up Spring Launcher
              .//
            ////////
         .////////////
       //////////////////
         ///////&&&&&&&&&**
            ////&&&&&&&&&*****
              //&&&&&&&&&*******
                 ****************
                   /***********
                      ******
                        /*
EOF

echo -n "Docker image name: "
read DOCKER_IMAGE

sed -i "s/docker_image=.*/docker_image=$DOCKER_IMAGE/g" gradle.properties

echo -n "Docker network: "
read DOCKER_NETWORK

docker network create "$DOCKER_NETWORK"

echo "Don't forget to update launch.json"