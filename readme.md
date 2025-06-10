Pré requisitos:
- Java 17
- Maven

Como rodar o projeto:
```bash
mvn clean install
```

Para executar um Peer
```bash
java -cp target/<NOME_DO_JAR>.jar peer.Main <TCP_PORT> <PEDACOS>
```
Exemplo:
```bash
java -cp target/Sockets_PeerToPeer_Torrent-1.0-SNAPSHOT.jar peer.Main 5001 1.txt 2.txt
```
