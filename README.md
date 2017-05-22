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

### Raspian auf SD Karte installieren

 - Raspian runterladen von: https://www.raspberrypi.org/downloads/raspbian/
 - `pv` installieren um dd Fortschritt anzuzeigen: `sudo apt-get install pv`
 - Auf SD Karte installieren mit `unzip -p 2017-04-10-raspbian-jessie-lite.zip | pv | sudo dd of=/dev/sda bs=4096` (ZIP-Dateiname und Device entsprechend anpassen natürlich!)
 - die /boot Partition mounten und:
 - mit `touch ssh` ssh aktivieren und mit
 - `wpa_passphrase SSID passphrase > wpa_supplicant.conf` das WLAN konfigurieren
 - SSD Karte in den PI einlegen und booten

### Pakete aktualisieren
 - `sudo apt-get update && sudo apt-get upgrade`
 
### raspi-config
 - `sudo raspi-config` ausführen
 - Hostname anpassen auf `whwpi`
 - Passwort ändern
 - Bei Localisation Options "de_DE@UTF8" aktivieren und de also default system locale wählen

### NFS Freigabe für Entwicklung einrichten

Für die Entwicklung habe ich die Files lokal auf meinem Entwicklungsrechner liegen und mounte sie per NFS vom PI:

```
mkdir /var/anemo
chmod 777 /var/anemo
sudo apt install nfs-kernel-server nfs-common joe 
echo "/var/anemo 10.1.0.10(rw,sync,no_subtree_check,no_root_squash)" | sudo tee /etc/exports
sudo service nfs-kernel-server restart
```

(10.1.0.10 ist der Entwicklungsrechner)

Hinweis: Raspbian scheint timing issues zu haben und portmap und nfs-kernel-server nicht in der richtigen Reihenfolge zu starten. Manchmal muss man `sudo service nfs-kernel-server restart` nach dem Booten des PIs manuell ausführen, damit die NFS Shares erreichbar sind.

### i2c
`sudo apt-get install i2c-tools`

#### configure: 
https://learn.adafruit.com/adafruits-raspberry-pi-lesson-4-gpio-setup/configuring-i2c

Kurzfassung:
 - `sudo raspi-config`
 - "Interface Options"
 - Enable I2C
 - Reboot

#### test:
`sudo i2cdetect -y 1`