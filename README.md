# simple-proxy
The purpose for this simple-proxy is to keep it running on a residential server so that we can access servers blocking known cloud IPs (like AWS).

This has been running on an old Raspberry Pi model B for a while, find the deployment instructions to do so below.

Surprisingly, 256 MB in RAM have been enough to maintain the app running for months until now.


## Why
When scrapping websites, you may find that everything just works until you deploy your scrapper to the cloud, a common reason is that some websites tend to have bot-detectors which usually blacklist any IP belonging to common cloud providers (like AWS, DigitalOcean, etc).

This project is a simple workaround to allow your scrapping server to scrape the problematic websites.

There is a dedicated [post](https://wiringbits.net/wiringbits/2020/06/07/a-raspberry-pi-as-a-decent-residential-proxy.html) with more details.


## Run
To run locally, [sbt](https://www.scala-sbt.org/) is required, once installed, just run `sbt run`, and start sending requests to `localhost:9000`, for example:

```shell script
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"url": "https://wiringbits.net", "headers": { "DNT": "1" }}' \
   http://localhost:9000
```

This request asks the proxy to issue a `GET` request to `https://wiringbits.net`, by sending the custom header `DNT: 1`.


## Deploy
Deploying the app requires three steps:
- Install the dependencies (Java 8).
- The actual app.
- A ssh tunnel to expose the app to the remote server where you will actually use it.

The simplified step list is:
- Build the app: `sbt dist`
- Copy the app to the residential server: `scp target/universal/simple-proxy-0.1.0-SNAPSHOT.zip pi@raspberrypi.local:~/`
- ssh into the server, then, run `unzip simple-proxy-0.1.0-SNAPSHOT.zip` and `cd simple-proxy-0.1.0-SNAPSHOT/ && ./bin/simple-proxy`
- ssh into the server, then, tunnel to our real server: `ssh -nNT -R 9999:localhost:9000 ubuntu@cazadescuentos.net`


### Deploy with systemd

#### simple-proxy
On the server, save it as `/etc/systemd/system/simple-proxy.service`:

```
[Unit]
Description=The simple-proxy
After=network.target

[Service]
ExecStart=/home/pi/simple-proxy-0.1.0-SNAPSHOT/bin/simple-proxy -Dhttp.port=9000 -Dpidfile.path=/dev/null
WorkingDirectory=/home/pi/simple-proxy-0.1.0-SNAPSHOT
StandardOutput=inherit
StandardError=inherit
RestartSec=1
StartLimitIntervalSec=0
Restart=always
User=pi

[Install]
WantedBy=multi-user.target
```

Then:
- Try it with `sudo service simple-proxy start`
- Enable it to run on the server startup: `sudo systemctl enable simple-proxy.service`


#### simple-proxy-tunnel
On the server, save it as `/etc/systemd/system/simple-proxy-tunnel.service`:

```
[Unit]
Description=The simple-proxy-tunnel
After=network.target

[Service]
ExecStart=/usr/bin/ssh -nNT -R 9999:localhost:9000 -o ConnectTimeout=10 -o ExitOnForwardFailure=yes -o ServerAliveInterval=180 ubuntu@cazadescuentos.net
WorkingDirectory=/home/pi
StandardOutput=inherit
StandardError=inherit
RestartSec=1
StartLimitIntervalSec=0
Restart=always
User=pi

[Install]
WantedBy=multi-user.target
```

Then:
- Try it with `sudo service simple-proxy-tunnel start`
- Enable it to run on the server startup: `sudo systemctl enable simple-proxy-tunnel.service`
