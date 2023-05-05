package seventeam.tgbot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import seventeam.tgbot.model.ShelterCat;
import seventeam.tgbot.model.ShelterDog;
import seventeam.tgbot.repository.ShelterCatRepository;
import seventeam.tgbot.repository.ShelterDogRepository;
import seventeam.tgbot.service.KeyBoardShelter;
import seventeam.tgbot.service.impl.DogServiceImpl;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static final String START = "/start";
    private final TelegramBot telegramBot;
    private final DogServiceImpl dogService;
    private ShelterCatRepository shelterCatRepository;
    private ShelterDogRepository shelterDogRepository;
    private KeyBoardShelter keyBoardShelter;


    public TelegramBotUpdatesListener(TelegramBot telegramBot, DogServiceImpl dogService) {
        this.telegramBot = telegramBot;
        this.dogService = dogService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    private boolean isCatNotDog = false;

    @Override
    public int process(List<Update> updates) {
        ShelterDog shelterDog = new ShelterDog();
        ShelterCat shelterCat = new ShelterCat();
        try {
            updates.stream()
                    .filter(update -> update.message() != null)
                    .forEach(update -> {
                        Message message = update.message();
                        Integer messageId = update.message().messageId();
                        Long chatId = message.chat().id();
                        String text = update.message().text();
                        String nameUser = update.message().chat().firstName();
                        switch (text) {
                            case START -> {
                                sendMassage(chatId, "Приветствую тебя, " + nameUser);
                                keyBoardShelter.chooseMenu(chatId);
                            }
                            case "\uD83D\uDC31 CAT" -> {
                                isCatNotDog = true;
                                keyBoardShelter.menu(chatId);
                                sendMassage(chatId, "Выбрана кошка");
                            }
                            case "\uD83D\uDC36 DOG" -> {
                                isCatNotDog = false;
                                keyBoardShelter.menu(chatId);
                                sendMassage(chatId, "Выбрана собака");
                            }
                            case "Главное меню", "Вернуться в главное меню" -> keyBoardShelter.menu(chatId);
                            case "Информация о приюте" -> keyBoardShelter.menuInfo(chatId);
                            case "Рассказать о нашем приюте" -> {
                                try {
                                    sendMassage(chatId, readFile("src/main/resources/draw/info.txt",
                                            StandardCharsets.UTF_8));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case "Взять питомца" -> sendMassage(chatId, "Такая возможность скоро будет добавлена");
                            case "Отчет" -> sendMassage(chatId, "Такая возможность скоро будет добавлена");
                            case "Позвать волонтера" -> sendMassage(chatId, "Такая возможность скоро будет добавлена");
                            case "Правила ухода за животными" -> {
                                try {
                                    sendMassage(chatId, readFile("src/main/resources/draw/care_of_animals.txt",
                                            StandardCharsets.UTF_8));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            default -> replyMessage(chatId, "Такой команды нет", messageId);
                        }
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMassage(Long chatId, String massage) {
        SendMessage sendMessage = new SendMessage(chatId, massage);
        telegramBot.execute(sendMessage);
    }

    private String readFile(String path, Charset encoding) throws IOException {
        return Files.readString(Paths.get(path), encoding);
    }

    public void replyMessage(Long chatId, String messageText, Integer messageId) {
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        sendMessage.replyToMessageId(messageId);
        telegramBot.execute(sendMessage);
    }
}
