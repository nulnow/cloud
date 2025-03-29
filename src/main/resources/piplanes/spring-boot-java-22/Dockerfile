FROM ubuntu:22.04


RUN apt update || exit
RUN apt -y install curl || exit
RUN apt -y install git || exit
RUN apt -y install openjdk-17-jdk || exit


COPY ./run.sh .

CMD ["./run.sh"]
