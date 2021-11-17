package com.sistemilab.idocs.service;

import com.sistemilab.idocs.model.Cliente;
import com.sistemilab.idocs.model.Utente;
import com.sistemilab.idocs.repository.ClienteRepository;
import com.sistemilab.idocs.repository.UtenteRepository;
import com.sistemilab.idocs.resource.CreateClienteRequest;
import com.sistemilab.idocs.resource.CreateUserRequest;
import org.jboss.logging.Logger;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/customers")
public class ClienteService {

    private static final Logger LOG = Logger.getLogger(ClienteService.class);

    @Inject
    ClienteRepository clienteRepository;
    @Inject
    UtenteRepository utenteRepository;

    public ClienteService(ClienteRepository clienteRepository, UtenteRepository utenteRepository) {
        this.clienteRepository  = clienteRepository;
        this.utenteRepository   = utenteRepository;
    }

    @GET
    @Path("/{userId}")
    public List<Cliente> list(@PathParam(value = "userId") String userId) throws Failure, WebApplicationException {
        Utente utente = utenteRepository.findById(Long.parseLong(userId));
        LOG.info("clienti dell'utente " + userId);
        LOG.info(utente.getClienti().stream().collect(Collectors.toList()));
        return utente.getClienti().stream().collect(Collectors.toList());
    }


    @GET
    @Path("/single/{customerId}")
    public Cliente getSingoloCliente(@PathParam(value = "customerId") String customerId) throws Failure, WebApplicationException {
        Cliente cliente = clienteRepository.findById(Long.parseLong(customerId));
        LOG.info("GET SINGOLO CLIENTE");

        return cliente;
    }

    @DELETE
    @Transactional
    @Path("/{idCliente}")
    public Response deleteCliente(@PathParam(value = "idCliente") String idCliente)throws Failure, WebApplicationException {
        LOG.info("CLIENTE DELETE START");
        try {
            Cliente cliente = clienteRepository.findById(Long.parseLong(idCliente));
            if (cliente != null)
                clienteRepository.delete(cliente);
            else
                return new ServerResponse("Cliente non presente in base dati", 500, new Headers<Object>());

        } catch (Exception e) {
            return new ServerResponse(e.getMessage(), 500, new Headers<Object>());
        }
        Response.ResponseBuilder rb = Response.ok();
        return rb.build();
    }

    @POST
    @Transactional
    public Response createCliente(@Valid CreateClienteRequest createClienteRequest) throws Failure, WebApplicationException {
        LOG.info("USER CREATION START");
        if(createClienteRequest.getIdUtente() == null){
            return new ServerResponse("ID UTENTE MANCANTE", 400, new Headers<Object>());
        }
        Cliente cliente = new Cliente();
                cliente.setRagioneSociale(createClienteRequest.getRagioneSociale());
                cliente.setPartitaIva(createClienteRequest.getPartitaIva());
                cliente.setNazione(createClienteRequest.getNazione());
                cliente.setDescrizione(createClienteRequest.getDescrizione());

        try {
            clienteRepository.create(cliente);
            LOG.info("CLIENTE CREATO CON ID: " + cliente.getId());

            Utente utente = utenteRepository.findById(createClienteRequest.getIdUtente());

            Set<Cliente> clienti = utente.getClienti();

            LOG.info("CLIENTI DELL'UTENTE " + createClienteRequest.getIdUtente() + " PRIMA DELL'AGGIORNAMENTO: " + clienti.stream().count());

            clienti.add(cliente);
            utente.setClienti(clienti);
            utenteRepository.persistAndFlush(utente);

            LOG.info("CLIENTI DELL'UTENTE " + createClienteRequest.getIdUtente() + " DOPO L'AGGIORNAMENTO: " + utente.getClienti().stream().count());

        } catch (Exception e) {
            return new ServerResponse(e.getMessage(), 500, new Headers<Object>());
        }

        Response.ResponseBuilder rb = Response.ok(cliente);
        return rb.build();
    }

}