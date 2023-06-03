package seventeam.tgbot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import seventeam.tgbot.enums.Status;
import seventeam.tgbot.model.*;
import seventeam.tgbot.service.impl.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final String START = "/start";
    private final String PASSWORD = "Password";
    private final TelegramBot telegramBot;
    private final DogServiceImpl dogService;
    private final CatServiceImpl catService;
    private final ClientServiceImpl clientService;
    private final KeyBoardService keyBoardService;
    private final ReportService reportService;
    private final VolunteerService volunteerService;
    private final Map<Long, Status> statuses = new HashMap<>();
    private boolean isCat = true;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, DogServiceImpl dogService, CatServiceImpl catService, ClientServiceImpl clientService, KeyBoardService keyBoardService, ReportService reportService, VolunteerService volunteerService) {
        this.telegramBot = telegramBot;
        this.dogService = dogService;
        this.catService = catService;
        this.clientService = clientService;
        this.keyBoardService = keyBoardService;
        this.reportService = reportService;
        this.volunteerService = volunteerService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.stream()
                    .filter(update -> update.message() != null)
                    .forEach(update -> {
                        Message message = update.message();
                        Long chatId = message.chat().id();
                        String text = message.text();
                        String firstName = message.chat().firstName();
                        String lastName = message.chat().lastName();
                        //Проверка статуса отчёта
                        if (statuses.containsValue(Status.REPORT_NOT_COMPILED)) {
                            reportService.createReport(update);
                            statuses.remove(chatId);
                        }
                        //Проверка наличия контакта
                        if (message.contact() != null) {
                            Contact contact = message.contact();
                            String phoneNumber = contact.phoneNumber();
                            clientService.createUser(contact.userId(), chatId, firstName, lastName, phoneNumber);
                            keyBoardService.chooseMenu(chatId);
                        }
                        //Проверка статуса отправки id питомца и валидация ввода целого числа
                        if (statuses.containsValue(Status.PET_ID_NOT_GET)) {
                            Pattern pattern = Pattern.compile("\\d+");
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                Client client = clientService.getUserByChatId(chatId);
                                volunteerService.sendToVolunteer(client, Integer.parseInt(matcher.group()));
                                sendMassage(chatId, "Заявка отправлена!");
                                statuses.remove(chatId);
                            } else {
                                sendMassage(chatId, "Вы не ввели id!");
                                keyBoardService.mainMenu(chatId);
                                statuses.remove(chatId);
                            }
                        }
                        //Проверка статуса отправки предупреждения и валидация ввода целого числа
                        if (statuses.containsValue(Status.SEND_WARNING)) {
                            Pattern pattern = Pattern.compile("\\d+");
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                Long ownerChatId = Long.valueOf(text);
                                statuses.remove(chatId);
                                sendMassage(ownerChatId, clientService.readFile("src/main/resources/draw/warning.txt"));
                            } else {
                                sendMassage(chatId, "Вы не ввели chatId владельца!");
                                keyBoardService.volunteerMenu(chatId);
                                statuses.remove(chatId);
                            }
                        }
                        if (text != null) {
                            switch (text) {
                                case START -> {
                                    sendMassage(chatId, "Приветствую тебя, " + firstName);
                                    keyBoardService.getContact(chatId);
                                }
                                case "\uD83D\uDC31 CAT" -> {
                                    isCat = true;
                                    sendMassage(chatId, "Выбран приют кошек");
                                    keyBoardService.mainMenu(chatId);
                                }
                                case "\uD83D\uDC36 DOG" -> {
                                    isCat = false;
                                    sendMassage(chatId, "Выбран приют собак");
                                    keyBoardService.mainMenu(chatId);
                                }
                                case "Главное меню", "Вернуться в главное меню" -> keyBoardService.mainMenu(chatId);
                                case "Информация о приюте" -> keyBoardService.infoMenu(chatId);
                                case "Рассказать о нашем приюте" ->{
                                    if (isCat) {
                                        sendMassage(chatId, clientService.readFile("src/main/resources/draw/info_shelter_cat.txt"));
                                    } else sendMassage(chatId, clientService.readFile("src/main/resources/draw/info_shelter_dog.txt"));
                                }

                                case "Взять питомца" -> {
                                    if (isCat) {
                                        sendMassage(chatId, catService.getAllPets().toString());
                                    } else sendMassage(chatId, dogService.getAllPets().toString());
                                    sendMassage(chatId, "Введите id питомца");
                                    statuses.put(chatId, Status.PET_ID_NOT_GET);
                                }
                                case "Отчет" -> {
                                    statuses.put(chatId, Status.REPORT_NOT_COMPILED);
                                    sendMassage(chatId, "Отправьте фото и отчёт одним сообщением");
                                }
                                case "Позвать волонтера" -> {
                                    if (volunteerService.getVolunteer(chatId) == null && clientService.getUserByChatId(chatId) != null) {
                                        volunteerService.callVolunteer(clientService.getUserByChatId(chatId).getPhoneNumber());
                                        sendMassage(chatId, "Скоро с вами свяжутся");
                                    } else sendMassage(chatId, "Забыл? Ты же сам волонтёр)");
                                }
                                case "Правила ухода за животными" -> sendMassage(chatId,
                                        clientService.readFile("src/main/resources/draw/care_of_animals.txt"));
                                case PASSWORD -> { //Сюда попадают желающие стать волонтёром
                                    if (clientService.getUserByChatId(chatId) != null && volunteerService.getVolunteer(chatId) == null) {
                                        Client client = clientService.getUserByChatId(chatId);
                                        volunteerService.createUser(client.getId(), chatId, client.getFirstName(),
                                                client.getLastName(), client.getPhoneNumber());
                                        clientService.deleteUser(client.getId());
                                    }
                                    keyBoardService.volunteerMenu(chatId);
                                }
                                case "Отправить предупреждение" -> {
                                    statuses.put(chatId, Status.SEND_WARNING);
                                    sendMassage(chatId, "Введите chatId владельца!");
                                }
                                case "Проверить отчет" -> {
                                    List<Report> reports = reportService.getAll();
                                    for (Report report : reports) {
                                        sendReport(chatId, report.getPhoto(), report.getReport());
                                    }
                                }
                            }
                            //Сообщения не содержащие текста кидают null сюда
                        } else logger.info("Метод message.text() возвращает ожидаемый null");
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

    private void sendReport(Long chatId, @NotNull File file, String report) {
        telegramBot.execute(new SendMessage(chatId, telegramBot.getFullFilePath(file)));
        telegramBot.execute(new SendMessage(chatId, report));
    }
}