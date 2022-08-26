package io.proj3ct.SpringGostBot.service;

import io.proj3ct.SpringGostBot.config.BotConfig;
import io.proj3ct.SpringGostBot.model.User;
import io.proj3ct.SpringGostBot.model.UserCompReposotory;
import io.proj3ct.SpringGostBot.model.UserForComp;
import io.proj3ct.SpringGostBot.model.UserReposotory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserCompReposotory userCompReposotory;

    @Autowired
    private UserReposotory userReposotory;
    final BotConfig config;

    static final  String HELP_TEXT = "Этот бот находит и отправляет ГОСТы и СНиПы в виде файла формата .pdf.\n\n" +
                                     "Вы можете выбрать команду в меню в нижнем левом углу или написать команду вручную:\n\n"+
                                     "Напишите /start что бы увидеть приветственное сообщение.\n\n" +
                                     "Напишите /getgost что бы получить ГОСТ в виде файла в формате .pdf.\n\n" +
                                     "Напишите /getsnip что бы получить СНиП в виде файла в формате .pdf.\n\n" +
                                     "Напишите /complaint что бы пожаловаться на бота.\n\n"+
                                     "Напишите /sentense что бы внести свое предложение по улучшению.\n\n" +
                                     "Напишите /help что бы увидеть это сообщение еще раз.\n\n";


    public TelegramBot(BotConfig config) {

        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "Получить приветственное сообщение"));
        listofCommands.add(new BotCommand("/getgost", "Получить файл ГОСТ"));
        listofCommands.add(new BotCommand("/getsnip", "Получить файл СНиП"));
        listofCommands.add(new BotCommand("/complaint", "Пожаловаться на бота."));
        listofCommands.add(new BotCommand("/sentence","Внести свое предложение по улучшению."));
        listofCommands.add(new BotCommand("/help", "Получить информацию о возможностях бота"));


        try{
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        }catch (TelegramApiException e){
            log.error("Ошибка при добавлении меню: " + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()){

            String textMassage = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (textMassage){
                case "/start":

                    registerUser(update.getMessage());
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/getsnip":
                    sendMessage(chatId, "Введите название СНиПа в формате СНиП X.XX.XX-XX, где Х-число\n\n"+
                               "Пример: СНиП 5.01.01-82");
                    break;
                case "/getgost":
                    sendMessage(chatId, "Введите название ГОСТа в формате ГОСТ Y X.XX.XX-XX, где X-число, Y-символ.\n\n"+
                                                   "Пример: ГОСТ Р 22.6.01-95");
                    break;
                case "/complaint":
                    sendMessage(chatId, "Для подачи жалобы введите <Жалоба: текст жалобы>\n\n"+
                                                   "Пример:        Жалоба: не находит ГОСТ Р 22.6.01-95.");
                    break;
                case "/sentence":
                    sendMessage(chatId, "Для подачи предложения введите <Предложение: текст предложения>\n\n"+
                                                   "Пример:   Предложение: добавьте недействующие ГОСТы.");
                    break;

                default:
                    if(textMassage.toLowerCase().startsWith("снип")){

                        File document = searchSnip(textMassage);

                        if(document == null){
                            sendMessage(chatId, "Данного СНиПа не существует или он уже не действителен.");
                        }else{
                            sendDocument(chatId, document);
                        }
                        break;


                    }
                    if(textMassage.toLowerCase().startsWith("гост")){
                        File document = searchGost(textMassage);

                        if(document==null){
                            sendMessage(chatId, "Данного ГОСТа не существует или он уже не действителен.");
                        }else{
                            sendDocument(chatId, document);
                        }
                        break;
                    }
                    if(textMassage.toLowerCase().startsWith("жалоб")){
                        complaintsAndSuggestions(update.getMessage());
                        sendMessage(chatId, "Спасибо, Ваша жалоба получена.\n\n"+
                                "Жалоба будет рассмотрена в ближайшее время");
                        break;
                    }
                    if(textMassage.toLowerCase().startsWith("предл")){
                        complaintsAndSuggestions(update.getMessage());
                        sendMessage(chatId, "Спасибо, Ваше предложение получено.\n\n"+
                                "Предложение будет рассмотрено в ближайшее время");
                        break;
                    }

                    sendMessage(chatId, "Извините, данной команды не существует, выберете команду в меню.");


            }
        }

    }


    private void registerUser(Message message) {

        if(userReposotory.findById(message.getChatId()).isEmpty()){
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userReposotory.save(user);
            log.info("Пользователь добавлен: " +user);
        }
    }

    private void complaintsAndSuggestions(Message message){
        var chatId = message.getChatId();
        var chat = message.getChat();


        UserForComp u1 = new UserForComp();
        double i = Math.random()*1000000;

        u1.setId((long)i);
        u1.setChatId(chatId);
        u1.setUserName(chat.getUserName());
        u1.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
        u1.setMassage(message.getText());
        userCompReposotory.save(u1);
        log.info("Жалоба или предложение добавлены в таблицу" + message);




    }


    private void startCommand(long chatId, String name){


        String answer = "Здравствуйте,  " +name + ",  рады Вас видеть";
        sendMessage(chatId, answer);
        log.info("Ответели пользователю: " +name);

    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("/start");
        row.add("/help");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/getgost");
        row.add("/getsnip");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/complaint");
        row.add("/sentence");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Случилась ошибка: " + e.getMessage());

        }
    }
    private File searchSnip(String textMassage) {
        String desiredFile = textMassage.toLowerCase().replaceAll("[^а-яА-Я0-9]", "");

        File file = new File("src/main/resources/SNIP");
        File[] fileList = file.listFiles();
        for (File f :
                fileList) {
            String fileName = f.getName().toLowerCase().replaceAll("[^а-яА-Я0-9]", "");
            if (fileName.equals(desiredFile)) {
                return new File(f.getAbsolutePath());
            }


        }
        return null;
    }

    private File searchGost(String textMassage){
        String desiredFile = textMassage.toLowerCase().replaceAll("[^а-яА-Я0-9]", "");

        File file = new File("src/main/resources/GOST");
        File[] filelist = file.listFiles();
        for (File f:
             filelist) {
            String fileName = f.getName().toLowerCase().replaceAll("[^а-яА-Я0-9]", "");
            if(fileName.equals(desiredFile)){
                return f;
            }
        }
        return null;
    }
    private void sendDocument(long chatId, File file) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(new InputFile(file));

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Случилась ошибка: " + e.getMessage());
        }
    }
}
