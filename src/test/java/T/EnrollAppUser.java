package T;

import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.gateway.Identities;

import java.nio.file.*;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class EnrollAppUser {

    public static void main(String[] args) throws Exception {
        // Percorso al wallet
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        // Se già presente, non serve aggiungerlo
        if (wallet.get("appUser") != null) {
            System.out.println("Identity appUser già presente nel wallet");
            return;
        }

        // Percorsi ai certificati di Org1 (generati da test-network)
        Path credentialPath = Paths.get("C:\\Users\\Aldo\\Desktop\\fabric-samples\\test-network\\organizations\\peerOrganizations\\org1.example.com\\peers\\peer0.org1.example.com\\msp");

        Path certPath = credentialPath.resolve(Paths.get("signcerts", "peer0.org1.example.com-cert.pem"));
        Path keyPath = credentialPath.resolve(Paths.get("keystore", "priv_sk"));
        X509Certificate cert = Identities.readX509Certificate(Files.newBufferedReader(certPath));
        PrivateKey key = Identities.readPrivateKey(Files.newBufferedReader(keyPath));

        Identity identity = Identities.newX509Identity("Org1MSP", cert, key);
        wallet.put("appUser", identity);

        System.out.println(">>> Identity appUser aggiunta al wallet!");
    }
}
