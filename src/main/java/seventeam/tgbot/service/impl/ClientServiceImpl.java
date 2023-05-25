package seventeam.tgbot.service.impl;

import org.springframework.stereotype.Service;
import seventeam.tgbot.model.Client;
import seventeam.tgbot.model.User;
import seventeam.tgbot.repository.ClientRepository;
import seventeam.tgbot.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ClientServiceImpl implements UserService {
    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void createUser(Long id, Long chatId, String firstName, String lastName, String phoneNumber) {
        Client client = new Client(id, chatId, firstName, lastName, phoneNumber);
        clientRepository.saveAndFlush(client);
    }

    public Client getUserByChatId(Long chatId) {
        return clientRepository.getByChatId(chatId);
    }

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    @Override
    public void updateUser(User user) {
        Client toUpdate = clientRepository.getReferenceById(user.getId());
        toUpdate.setFirstName(user.getFirstName());
        toUpdate.setLastName(user.getLastName());
        toUpdate.setPhoneNumber(user.getPhoneNumber());
        clientRepository.saveAndFlush(toUpdate);
    }

    @Override
    public void deleteUser(Long id) {
        clientRepository.deleteById(id);
    }

    public String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
    }
}
