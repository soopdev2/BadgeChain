package servlet;

import org.hyperledger.fabric.gateway.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Badge;
import entity.BadgeGenerator;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IssueBadgeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ObjectMapper mapper = new ObjectMapper();

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String issuer = req.getParameter("issuer");
            String recipient = req.getParameter("recipient");

            File imgFile = File.createTempFile("badge_", ".png");
            File jsonFile = File.createTempFile("badge_", ".json");
            Badge badge = BadgeGenerator.generate(name, description, issuer, recipient, imgFile, jsonFile);

            Path walletPath = Paths.get("C:", "Users", "Salvatore", "Desktop", "BadgeChain", "wallet");

            Wallet wallet = Wallets.newFileSystemWallet(walletPath);

            String identityName = "appUser";
            boolean identityFound = wallet.get(identityName) != null;
            System.out.println("DEBUG: Wallet path: " + walletPath.toAbsolutePath());
            System.out.println("DEBUG: Identity '" + identityName + "' found in wallet: " + identityFound);

            if (!identityFound) {
                System.err.println("FATAL DEBUG: The identity '" + identityName + "' is missing! Check wallet initialization logic.");
            }

            Path networkConfigPath = Paths.get("C:", "Users", "Salvatore", "Desktop", "fabric-samples", "test-network", "organizations",
                    "peerOrganizations", "org1.example.com", "connection-org1.yaml");

            Gateway.Builder builder = Gateway.createBuilder()
                    .identity(wallet, "appUser")
                    .networkConfig(networkConfigPath)
                    .discovery(true);

            try (Gateway gateway = builder.connect()) {
                Network network = gateway.getNetwork("mychannel");
                Contract contract = network.getContract("badge-contract");

                contract.submitTransaction("issueBadge",
                        badge.id, badge.jsonHash, badge.issuer, badge.recipient);
            }

            resp.getWriter().write(mapper.writeValueAsString(badge));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
