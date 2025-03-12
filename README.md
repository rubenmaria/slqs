# slqs a Simple Local Quick Share
A simple tool to quickly share files and directories locally written in Java.

## Usage 
```
slqs [Command]
Commands:
 send [Flag] [Paramter]	sends a file or directory
 receive [Optional]	receives a file or directory
 help			displays help 
Paramter:
 --host, -h [HOST]	local adress of the receiver
 --path, -p [PATH]	path to object you want to share
Flags: 
 --force, -f		removes receiving prompt
 --recursive, -r	recursively send a directory
Optional:
 --port [PORT]		communication port
```

## Build && Run
```
mvn clean package
java -jar target/slqs-1.0-jar-with-dependencies.jar
```

<video src='https://github.com/user-attachments/assets/84411fb3-9e64-435f-ae58-602d087b0544' width=180/>
