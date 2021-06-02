# About

The daemon that configures the local host from a foilen-ui service.

# More information

## Docker-sudo

Description: This application is creating users and is installing applications that they run. Since everything is running with Docker and we do not want to give full access to it to everyone while giving them access to their running application, this application is configuring *docker-sudo* in a way that each user can manage their containers.

Installation:

```bash
cd /tmp
wget https://deploy.foilen.com/docker-sudo/docker-sudo_1.3.2_amd64.deb
dpkg -i docker-sudo_1.3.2_amd64.deb
```

# Usage

## Configuration environment

- *CONFIG_FILE*: The configuration file (or can be passed as --configFile in the arguments)
- *HOSTFS*: The local path to the mounted volume that is the root of the host's filesystem

## Configuration file

You need to create a json configuration file that maps the object DockerManagerConfig.

Here is an example of the content:

```json
{
  "internalDatabasePath" : "/var/infra-docker-manager/db",
  "persistedConfigPath" : "/var/infra-docker-manager/persistedConfig",
  "imageBuildPath" : "/var/infra-docker-manager/imageBuild"
}
```

You can then specify the full path of that file as the *configFile* argument when launching the app or as the
*CONFIG_FILE* environment variable.

## Initial MachineSetup file

In persisted folder, you need a file named *machineSetup.json*. It must contains at least the details to contact the remote InfraUi and the machine's name to refresh the state periodically ; else, will just configure once with the stored details.

```json
{
  "uiApiBaseUrl" : "https://infra-ui.example.com",
  "uiApiCert" : null,
  "uiApiUserId" : "aaabbbccc",
  "uiApiUserKey" : "dddeeefff",
  "machineName" : "node1.example.com"
}
```

# App Test Docker

## Launch the application for testing in Docker (locally)


```bash
# Compile and create image
./create-local-release.sh

DATA_PATH=$(pwd)/_data
INTERNAL_DATABASE_PATH=$DATA_PATH/db
PERSISTED_CONFIG_PATH=$DATA_PATH/persistedConfig
IMAGE_BUILD_PATH=$DATA_PATH/imageBuild
mkdir -p $INTERNAL_DATABASE_PATH $PERSISTED_CONFIG_PATH $IMAGE_BUILD_PATH

# Create config
cat > $DATA_PATH/config.json << _EOF
{
  "internalDatabasePath" : "/data/db",
  "persistedConfigPath" : "/data/persistedConfig",
  "imageBuildPath" : "/data/imageBuild"
}
_EOF

# Create machinesetup
cat > $DATA_PATH/persistedConfig/machineSetup.json << _EOF
{
  "unixUsers" : [ {
    "homeFolder" : "/home/testing",
    "id" : 2000,
    "name" : "testing",
    "shell" : "/bin/bash"
  } ],
  "applications" : [ {
    "name" : "mariadb",
    "executionPolicy" : "ALWAYS_ON",
    "applicationDefinition" : {
      "_nextAssetId" : 1,
      "from" : "mariadb:10",
      "environments" : {
        "MYSQL_ROOT_PASSWORD" : "qwerty"
      },
      "volumes" : [
      	{
      		"hostFolder" : "/home/testing/data",
      		"containerFsFolder" : "/var/lib/mysql/",
      		"ownerId" : 2000, "groupId" : 2000, "permissions": "750"
      	}
      ],
      "portsExposed" : {
        "13306" : 3306
      },
      "containerUsersToChangeId" : [ { "a" : "mysql", "b" : 2000 } ],
      "runAs" : 2000
    }
  } ]
}
_EOF

# Execute
docker run -ti \
  --rm \
  --env HOSTFS=/hostfs/ \
  --env CONFIG_FILE=/data/config.json \
  --volume $DATA_PATH:/data \
  --volume /:/hostfs/ \
  --volume /usr/bin/docker:/usr/bin/docker \
  --volume /usr/lib/x86_64-linux-gnu/libltdl.so.7.3.1:/usr/lib/x86_64-linux-gnu/libltdl.so.7 \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  --hostname $(hostname -f) \
  --workdir /data \
  foilen-infra-docker-manager:master-SNAPSHOT

# Check the db files
sudo find /home/testing/data

# Try docker-sudo
sudo su testing
docker-sudo
docker-sudo ps
docker-sudo logs mariadb
docker-sudo tails mariadb
docker-sudo bash mariadb
	mysql -u root -pqwerty
		show databases;
		create database testing;
		show databases;

# Cleanup
docker stop mariadb
sudo deluser testing
sudo rm -rf _data /home/testing

```
