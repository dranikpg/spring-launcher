Ktor application made for quickly demonstrating Spring projects on our Java lessons. 

<img src="https://github.com/dranikpg/spring-launcher/blob/media/launch.png" width="800px"/>

Spring launcher:
* Starts a fresh Postgres container
* Builds the project
* Injects a fitting database configuration
* Runs the project in an exposed docker container
* Forwards all logs to the website in real-time

<img src="https://github.com/dranikpg/spring-launcher/blob/media/output.png" width="500px"/>

### How to deploy spring launcher on your host

Run  `configure.sh` for setting the docker image, network name, exposed port and channel/stream buffer sizes.

Next run `gradle prepare` for building the docker launcher image and the React frontend

Now you're ready to run `gradle run` for starting the launcher

