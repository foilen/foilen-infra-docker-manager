# About

The daemon that configures the local host from a foilen-ui service.

# Usage

## Configuration file TODO

You need to create a json configuration file that maps the object InfraUiConfig.

Here is an example of the content:

```json
{
}
```

You can then specify the full path of that file as the *configFile* argument when launching the app or as the
*CONFIG_FILE* environment variable.

## Initial MachineSetup file TODO

In persisted folder named *machineSetup.json*. Must have at least the details to contact the remote InfraUi to refresh the state periodically ; else, will just configure once.

## Configuration environment TODO

- *CONFIG_FILE*: The configuration file
- *HOSTFS*: The local path to the mounted volume that is the root of the host's filesystem
