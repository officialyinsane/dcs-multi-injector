# DCS Multi Injector

## What is it?

Full credit to [@Dzsek](https://github.com/Dzsek) for the [VS Code DCS-Lua-Injector](https://github.com/Dzsek/vs-code-dsc-injector/) - if you use that Lua Injector for DCS, and manage/work on multiple DCS servers, the Multi-Injector was written for you!

The Multi-Injector will allow you to inject Lua code into remote DCS instances, and let you choose which instance you inject to.

## Roadmap

- [X] Lua injection into a locally running DCS instance
- [X] Lua injection into a remotely running DCS instance
- [ ] Docker image push and CI/CD pipelines
- [ ] DCS Server Information Editor (to configure Multi-Injector easier)
- [ ] OAuth2 (Discord) to allow RBAC and server grouping, then allowing the Multi-Injector to be web-based
- [ ] OAuth2 (Google) - as above
- [ ] Fork this project and use for secret squirrel stuff & things (won't be open sourced).

## Known issues

1. The localhost installation of the injector must have the default password, provided by the upstream project.
2. Editor exists but is non-functional at the moment - WIP.

## How to run

1. See the [docker-compose.yml](./docker-compose.yml)
2. Realise that you don't have access to my private Docker Registry - don't ask - won't be given.
3. Refer to How To Build.
4. Navigate to the [Discord Developer Console](https://discord.com/developers/applications) and create yourself an Application (you'll need it for authentication)
   1. Remember to navigate to OAtuh2, and add a Redirect for `http://localhost:8080/login/oauth2/code/discord` (if running locally)
   2. Also add any Redirects needed for your hosting domain. 
5. Create a `.env` file next to the `docker-compose.yml` with something along the lines of:
```
OAUTH_DISCORD_APPLICATION_ID=<your-client-id>
OAUTH_DISCORD_APPLICATION_NAME=<your-client-name>
OAUTH_DISCORD_APPLICATION_SECRET=<your-client-secret>
POSTGRES_USER=some-user
POSTGRES_PASSWORD=a-better-password-than-this
```
6. Start the dcs-injector-ui container.
7. Navigate to [http://localhost:8080/dcs-injector](http://localhost:8080/dcs-injector).
8. Copy/pasta some Lua code in.
9. Hit play.
10. Profit.

## How to add servers

This will be needed until I have the Editor up and running:
1. You just noticed that there's only ever one server in the list, Localhost, and are wondering how to manage multiple.
2. Connect to the Postgres database (see docker-compose.yml and "How to run" for authentication)
3. Execute the following SQL in the database
```sql
INSERT INTO injector.dcs_server (name, hostname, port, password) VALUES
    ('Server1', 'server.domain-name.com', 18080, 'ThisIsNotARealServerOrPassword')
    ;
```

## How to build (for developers)

1. If you're not a Java developer - apologies, find one and give them this document.
2. See the [pom.xml](./pom.xml) - change the property `project-docker-image.name` to reflect a Container Registry where you have permissions to pull/push.
3. Run `mvn clean package -Pproduction`
4. Manually push the Docker Image (if desired) to your chosen repository.

Please note, when testing in your IDE, remember to setup the required environment variables (see How to run, above).

## Licence

None - feel free to do as you wish (so long as it's legal where you live).

## How to contribute

I'm not particularly looking for contributors, but if you want to fork it, I can be bribed into helping you out along the way in return for the cost of a coffee. 
If you know how to fork it and how to write the Java/SQL for it, you can work out how to contact me about buying coffee too. ;)