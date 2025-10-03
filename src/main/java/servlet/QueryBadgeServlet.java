package servlet;

import org.hyperledger.fabric.gateway.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QueryBadgeServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            String badgeId = req.getParameter("id");

            Path walletPath = Paths.get("wallet");
            Wallet wallet = Wallets.newFileSystemWallet(walletPath);
            Path networkConfigPath = Paths.get("..", "test-network", "organizations",
                    "peerOrganizations", "org1.example.com", "connection-org1.yaml");

            Gateway.Builder builder = Gateway.createBuilder()
                    .identity(wallet, "appUser")
                    .networkConfig(networkConfigPath)
                    .discovery(true);

            try (Gateway gateway = builder.connect()) {
                Network network = gateway.getNetwork("mychannel");
                Contract contract = network.getContract("badge-contract");

                byte[] queryResult = contract.evaluateTransaction("queryBadge", badgeId);
                resp.getWriter().write(new String(queryResult));
            }

        } catch (IOException | ContractException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
            System.out.println(e);
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
