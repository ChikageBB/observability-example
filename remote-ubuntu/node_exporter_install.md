Login:
```bash
ssh root@localhost -p 2222
```

Check version:
```bash
lsb_release -a
```

Get node exporter archive
```bash
wget https://github.com/prometheus/node_exporter/releases/download/v1.9.1/node_exporter-1.9.1.linux-amd64.tar.gz
```

Decompress
```bash
tar -xvf node_exporter-1.9.1.linux-amd64.tar.gz
```

Replace node exporter
```bash
mv node_exporter-1.9.1.linux-amd64/node_exporter /usr/local/bin/
```

Start node exporter
```bash
nohup node_exporter &
```

Check metrics
```bash
curl http://localhost:9100/metrics
```
