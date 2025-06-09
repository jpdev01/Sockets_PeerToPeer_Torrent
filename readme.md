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
java -cp target/peer-to-peer.jar peer.Main 127.0.0.1:5001 1.txt 2.txt
```