# ANEMO Wetterstation für den Wassersportverein Heidelberg West

## Projektbeschreibung

Das Projekt befindet sich noch in der Entwicklung, an dieser Stelle nur ein paar grundlegende Informationen zusammengefasst:

 - Die Wetterstation läuft auf einem Raspberry PI. Auf diesem läuft Raspian Jessie.
 - Die eigentliche Anwendung ist in Java geschrieben und läuft in einem privilegierten Docker Container
 - Die GPIO Pins werden mit der Bibliothek PI4J (http://pi4j.com/) angesprochen
 - Auf dem PI läuft im Moment ein Webserver um direkt Daten auszugeben
 
## Dateien

 - `anemo.fzz` - Fritzing (http://fritzing.org/home/) Aufbau der Hardware
 - `build-docker-image.sh` - Bash Script um das Docker Image auf dem PI zu bauen
 - `Dockerfile` - Dockerfile für den Java 8 Container
 - `run-dev.sh` - Zum Starten der Wetterstation über Gradle Task
 
## Raspberry PI Setup

### NFS Freigabe einrichten 

(10.1.0.10 ist der Zielrechner)

```
mkdir /var/anemo
chmod 777 /var/anemo
sudo apt install nfs-kernel-server nfs-common joe 
echo "/var/anemo 10.1.0.10(rw,sync,no_subtree_check,no_root_squash)" | sudo tee /etc/exports
sudo service nfs-kernel-server restart
```

Hinweis: Raspbian scheint timing issues zu haben und portmap und nfs-kernel-server nicht in der richtigen Reihenfolge zu starten. Manchmal muss man `sudo service nfs-kernel-server restart` nach dem Booten des PIs manuell ausführen, damit die NFS Shares erreichbar sind.

### i2c
`sudo apt-get install i2c-tools`

#### configure: 
https://learn.adafruit.com/adafruits-raspberry-pi-lesson-4-gpio-setup/configuring-i2c

#### test:
`sudo i2cdetect -y 1`