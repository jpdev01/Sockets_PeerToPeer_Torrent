Pré requisitos:
- Java 17
- Maven

Como rodar o projeto:
```bash
mvn clean install
```

Para executar um Peer
```bash
java -cp target/<NOME_DO_JAR>.jar peer.Main <PEER_ID> <PEDACOS>
```
Exemplo:
```bash
java -cp target/Sockets_PeerToPeer_Torrent-1.0-SNAPSHOT.jar peer.Main 127.0.0.1:5001 1.txt 2.txt
```