# node
This is the repository for the node component.

## Useful links
Cloud repo: https://bitbucket.org/DennisHak/node_netwerk_submarine_cloud/src/master/ (ANY CHANGES IN THE CLOUD REPO WILL BE OVERWRITTEN, AND THUS REMOVED)    
Sonar:      https://sonarcloud.io/dashboard?id=Dennis-Hakvoort_node_cloud

## Before starting
Install the nescessary dependencies using:
`pip3 install -r requirements.txt`

## 'Module 'submarine' does not exist'
This error occurs because the python path does not include a path to this project's root directory. You can add the path by
running the following command:    
`export PYTHONPATH=[path to root directory]`    
Please note that the path cannot be a relative path, but must be a full path (You can get the full path by running `pwd` while
in the root directory.)    

So, for example, the command can look like this:    
`export PYTHONPATH=/home/student/Programming/node`
    
    

## How to start
#### NodeRouting
The NodeRouting needs two variables to be started. An IP and a port. These are given with the -i and -p tags in the command. For example:    
`python3 ./submarine/ConnectionModule/src/NodeRouting.py -i 127.0.0.1 -p 25010`    
    
If you're using intellij, the following setup will do the same:
![image](https://i.imgur.com/cGtGjuY.png)
    
_Please note, the NodeRouting needs to be ran from the project root (where this file is located), otherwise you will get an error similar to the following:_
`FileNotFoundError: [Errno 2] No such file or directory: './ConnectionModuleNode/NodeRoutingSchema.json'`
    
####ClientNode
ClientNode currently doesnt ask for any variables, they're all hard coded into the python file. Please set the variables as you'd
like them to be in this file directly. It can be ran without any special flags or specific working directories:    
`python3 ClientNode.py `

## Nodes on VPS
There are three nodes deployed on VPS servers. Information regarding to these nodes is documented below*:

### Node 1
Name: ASD-P1-2020-Server1

FQDN: asd-p1-server1.asd.icaprojecten.nl

WAN IP: 94.124.143.122

LAN IP: 10.20.3.122 

### Node 2
Name: ASD-P1-2020-Server2

FQDN: asd-p1-server2.asd.icaprojecten.nl

WAN IP: 94.124.143.132

LAN IP: 10.20.3.132 

### Node 3
Name: ASD-P1-2020-Server3

FQDN: asd-p1-server3.asd.icaprojecten.nl

WAN IP: 94.124.143.139

LAN IP: 10.20.3.139 

*Usernames and passwords are documented in wegwijspiet on Confluence.

### Additional information
To manage the server, the use of SSH is required. This can be done using
`ssh <user>@<FQDN>`

To get the most recent code from bitbucket, use
`cd ~ && git clone https://sanbak@bitbucket.org/DennisHak/node_netwerk_submarine_cloud.git` 

To execute the code, execute `nohup ./run.sh &` and close the terminal. 

`run.sh` is a script which starts up the docker image and runs NodeRouting.py
````
docker start node-svr1
````

You can communicate with the nodes using a socket connection at `<FQDN>` on port `25010`. Input will be validated before it gets processed.

## Docker
_These commands are a very rough guide, for those not familiar with docker. As always, there are more ways to Rome_

This project can be ran in a docker image. This can be done by building the image:    
`docker build --tag asd-node:1.0 .`    
this command will make an image with the tag 'asd-node:1.0'. It can then be ran with the command:    
`docker run --publish 25010:25010 -e "PORT=25010" --name node asd-node:1.0`    
This will start the image on localhost with port 25010, the port can be changed in the command (`-e "PORT=[portnr]`)    
To stop the container, use the following command:    
`docker container stop node`
To restart, you need to remove the image first. You can prune all images by using:
`docker container prune`
