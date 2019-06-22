package Bin;

import Bin.Audio.AudioClient;
import Bin.GUI.ActionsBox;
import Bin.GUI.Forms.MainFrame;
import Bin.Networking.ClientController;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Protocol.DataPackagePool;
import Bin.Networking.Server;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.Call;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Writers.BaseWriter;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * My interpretation of the whole system
 * Sure you can do better
 */

public class Main implements ErrorHandler {


    private final ActionsBox actionsBox;
    private MainFrame mainFrame;
    private final ClientController controller;
    private Server server;
    private final Map<Integer, BaseUser> users;
    private final AudioClient audioClient;
    private final Call callDialog;

    /**
     * Needs for single purpose
     * May fix glitches in sound
     * But need tests
     */

    private final Executor executor;

    private Main() {
        AbstractDataPackagePool.init(new DataPackagePool());

        audioClient = AudioClient.getInstance();
        users = new HashMap<>();
        callDialog = new Call();
        actionsBox = new ActionsBox();
        controller = new ClientController(this);
        executor = Executors.newSingleThreadExecutor();

        initialInitForActions();
        EventQueue.invokeLater(() -> {
            mainFrame = new MainFrame(actionsBox);
            controller.getProcessor().addListener(usersIncome());
            controller.getProcessor().addListener(showMessage());
            controller.getProcessor().addListener(callHandler());
            controller.getProcessor().addListener(audioHandler());
        });
    }

    /**
     * Default init of all back end actions
     */

    private void initialInitForActions() {
        actionsBox.updateConnect(connect());
        actionsBox.updateCreateServer(createServer());
        actionsBox.updateNameAndId(nameAndId());
        actionsBox.updateDisconnect(disconnect());
        actionsBox.updateCallForUsers(callForUsers());
        actionsBox.updateSendMessage(sendMessage());
        actionsBox.updateCallSomeOne(callSomeOne());
        actionsBox.updateCancelCall(cancelCall(true));
        actionsBox.updateAcceptCall(acceptCall());
        actionsBox.updateDenyCall(denyCall());
        actionsBox.updateMute(muteAction());
        actionsBox.updateEndCall(endConversation());
        actionsBox.updateChangeMultiplier(audioClient.changeMultiplier());

    }

    public static void main(String[] args) {
//        Main main = new Main();
//        System.setProperty("java.util.logging.config.file", "src\\main\\resources\\properties\\logging.properties");
//        LogManager.getLogManager().readConfiguration();
        new Main();
//        new Main();

    }

    /**
     * Tries to connect uses on First skin
     * String[0] name
     * String[1] host name / ip
     * String[2] port
     * <p>
     * return true or false in case of success or failure
     * and null in case of exception
     *
     * @return ready to call function
     */

    private Function<String[], Boolean> connect() {
        return strings -> {
            try {
                return controller.connect(strings[0], strings[1], strings[2]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    /**
     * Tries to create server on First skin
     * String[0] port
     * String[1] sample rate
     * String[2] sample size
     * <p>
     * You can't create new server if you already have one must restart the app
     *
     * @return ready yo use action
     * true if created false otherwise and null if port already in use by other app
     */

    private Function<String[], Boolean> createServer() {
        return strings -> {
            try {
                if (server == null) {
                    server = new Server(strings[0], strings[1], strings[2]);
                    server.start("Server");
                    return true;
                }
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                server = null;
                return null;
            }
        };
    }

    /**
     * Uses to indicate your name and id
     * when Second skin is created
     * Obtain id only from the server
     *
     * @return ready to use name and id provider
     */

    private Supplier<String> nameAndId() {
        return () -> controller.getMe().toString();
    }

    /**
     * Actions for the disconnect button on second skin
     *
     * @return ready to use action
     */

    private Runnable disconnect() {
        return () -> {
            controller.disconnect();
            audioClient.close();

        };
    }

    /**
     * Send call for users
     *
     * @return ready to call function
     */

    private Runnable callForUsers() {
        return () -> controller.getWriter().writeUsersRequest(controller.getMe().getId());
    }

    /**
     * Creates a handler for users income
     * gets users from string
     *
     * @return ready to be established handler
     */

    private Consumer<AbstractDataPackage> usersIncome() {
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_USERS)) {
                BaseUser[] baseUsers = BaseUser.parseUsers(baseDataPackage.getDataAsString());
                users.clear();
                Arrays.stream(baseUsers).forEach(baseUser -> users.put(baseUser.getId(), baseUser));
                mainFrame.updateUsers(baseUsers);
            }
        };
    }

    /**
     * Creates a function that send a message to someone
     *
     * @return ready to use function
     */

    private BiConsumer<Integer, String> sendMessage() {
        return (integer, s) -> controller.getWriter().writeMessage(controller.getMe().getId(), integer, s);
    }

    /**
     * Creates message handler
     * displays it
     *
     * @return ready to be registered handler
     */

    private Consumer<AbstractDataPackage> showMessage() {
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_MESSAGE)) {
                String message = baseDataPackage.getDataAsString();
                int index = retrieveMessageMeta(message);
                if (index != -1) {
                    audioClient.playIndexedMessageSound(index);
                } else {
                    audioClient.playRandomMessageSound();
                }
                mainFrame.showMessage(users.get(baseDataPackage.getHeader().getFrom()), message);
            }
        };
    }

    /**
     * Parse incoming message string
     * Tries to find <$[0-9]+?>
     * And get those digits for the index
     *
     * @param message to parse
     * @return -1 in case if there is no such thing otherwise appropriate value
     */

    private static int retrieveMessageMeta(String message) {
        Pattern pattern = Pattern.compile("<\\$\\d+?>");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String rawData = matcher.group();
            return Integer.valueOf(rawData.replaceAll("(<\\$)|(>)", ""));
        }
        return -1;
    }

    /**
     * Create a ready to use function
     * when you need to call some one
     *
     * @return ready to use
     */

    private Consumer<BaseUser> callSomeOne() {
        return baseUser -> {
            callDialog.setCalling(true, false);
            callDialog.setReceiver(baseUser);
            controller.getWriter().writeCall(controller.getMe().getId(), baseUser.getId());
        };
    }

    /**
     * Creates ready to use action for cancelling an incoming call
     *
     * @param isYou needs to separate auto cancel and hand cancel
     * @return ready to use function
     */

    private Consumer<BaseUser> cancelCall(boolean isYou) {
        return baseUser -> {
            controller.getWriter().writeCancel(controller.getMe().getId(), baseUser.getId());
            if (isYou) {
                callDialog.setCalling(false);
            }
        };
    }

    /**
     * Creates ready to use function for accept incoming call
     * BaseUser[0] = is who calls you
     * others are who in conversation with him
     *
     * @return ready to use function
     */

    private Consumer<BaseUser[]> acceptCall() {
        return baseUsers -> {
            int from = baseUsers[0].getId();

            startConv(baseUsers);

            controller.getWriter().writeAccept(controller.getMe().getId(), from);

            callDialog.setCalling(false);
        };
    }

    /**
     * Creates ready to use action for deny call
     *
     * @return ready to use
     */

    private Consumer<BaseUser> denyCall() {
        return baseUser -> {
            controller.getWriter().writeDeny(controller.getMe().getId(), baseUser.getId());
            callDialog.setCalling(false);
        };
    }

    /**
     * Creates a call handler
     * Reacts to all call actions
     * If you are already in calling then according to
     * who you try to call it will auto accept or cancel
     * the incoming call
     * Starts a conversation if accepted
     * other cases just dispose the dialog with a message
     *
     * @return ready to be registered handler
     */

    private Consumer<AbstractDataPackage> callHandler() {
        return baseDataPackage -> {
            switch (baseDataPackage.getHeader().getCode()) {
                case SEND_CALL: {
                    int from = baseDataPackage.getHeader().getFrom();
                    if (callDialog.isCalling()) {
                        //auto accept and cancel
                        BaseUser receiver = callDialog.getReceiver();
                        if (receiver.getId() == from) {
                            BaseUser[] users;
                            if (baseDataPackage.getHeader().getLength() > 0) {
                                BaseUser[] parseUsers = BaseUser.parseUsers(baseDataPackage.getDataAsString());
                                users = new BaseUser[parseUsers.length + 1];
                                users[0] = receiver;
                                System.arraycopy(parseUsers, 0, users, 1, parseUsers.length);
                            } else {
                                users = new BaseUser[]{receiver};
                            }
                            actionsBox.acceptCall().accept(users);
                            //auto accept call handle here
//                            mainFrame.closeCall("Call was accepted");
                            mainFrame.showDialog("Call was accepted");
                        } else {
                            BaseUser caller = users.get(from);
                            //auto cancel call because you are busied already
                            cancelCall(false).accept(caller);   //not from actionBox because of boolean argument
                            mainFrame.showMessage(caller, "I called you, but you were busy, call me");
                        }
                    } else {
                        //ordinary 1 side call
                        callDialog.setCalling(true, true);
                        callDialog.setReceiver(users.get(baseDataPackage.getHeader().getFrom()));
                        mainFrame.showIncomingCall(users.get(baseDataPackage.getHeader().getFrom()).toString(), baseDataPackage.getDataAsString());
                    }
                    break;
                }
                case SEND_CANCEL: {
                    if (callDialog.isCalling() && baseDataPackage.getHeader().getFrom() == callDialog.getReceiver().getId()) {
                        mainFrame.closeCall("Call was canceled");
                        callDialog.setCalling(false);
                    }
                    break;
                }
                case SEND_APPROVE: {
                    if (callDialog.isCalling() && baseDataPackage.getHeader().getFrom() == callDialog.getReceiver().getId()) {
                        mainFrame.closeCall("Call was accepted");
                        callDialog.setCalling(false);
                        startConv(baseDataPackage);
                    }
                    break;
                }
                case SEND_DENY: {
                    if (callDialog.isCalling() && baseDataPackage.getHeader().getFrom() == callDialog.getReceiver().getId()) {
                        mainFrame.closeCall("Call was denied");
                        callDialog.setCalling(false);
                    }
                    break;
                }
            }
        };
    }

    /**
     * Start conversation
     * firstly check if there people in incoming conf
     * than open sound source lines
     * and at the end display it all n gui
     *
     * @param allUsers incoming users from the call can be only 1 person
     *                 who called you
     */

    private void startConv(BaseUser[] allUsers) {
        int from = allUsers[0].getId();
        if (allUsers.length > 1) {
            int[] indexes = new int[allUsers.length + 1];
            for (int i = 0; i < allUsers.length; i++) {
                indexes[i] = allUsers[i].getId();
            }
            indexes[indexes.length - 1] = from;
            audioClient.startConversation(sendSoundAction(), indexes);
            mainFrame.startConversation(users.get(from).toString(), audioClient.getSettings(from));
            //add others on GUI, call after first loop because they don't have controls for audio
            for (int i = 1; i < allUsers.length; i++) {
                mainFrame.addToConv(allUsers[i].toString(), audioClient.getSettings(allUsers[i].getId()));
            }
        } else {
            audioClient.startConversation(sendSoundAction(), from);
            mainFrame.startConversation(allUsers[0].toString(), audioClient.getSettings(from));
        }
    }

    /**
     * Prepare string full of BaseUser.toString()+'\n'
     * convert in to appropriate array
     *
     * @param from      who calls
     * @param convUsers the boys in conversation
     */

    private void startConv(int from, String convUsers) {
        BaseUser caller = users.get(from);
        BaseUser[] result;
        if (convUsers.length() > 0) {
            BaseUser[] parseUsers = BaseUser.parseUsers(convUsers);
            result = new BaseUser[parseUsers.length + 1];
            result[0] = caller;
            System.arraycopy(parseUsers, 0, result, 1, parseUsers.length);
        } else {
            result = new BaseUser[]{caller};
        }
        startConv(result);
    }

    /**
     * Retrieve info for startConv(int, String) method
     * Server will add conversation info if so exists
     *
     * @param baseDataPackage incoming package
     */

    private void startConv(AbstractDataPackage baseDataPackage) {
        startConv(baseDataPackage.getHeader().getFrom(), baseDataPackage.getDataAsString());
    }

    /**
     * Uses only for AudioCapture class
     *
     * @return action for writing sound data
     */

    private Consumer<byte[]> sendSoundAction() {
        return bytes -> executor.execute(() -> {
            try {
                controller.getWriter().writeSound(controller.getMe().getId(), bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create audio handler
     * Handles all events corresponding to sound
     * It adds new sound lines and remove already existed
     * and stop a conversation
     * <p>
     * Also clears all existed packages
     *
     * @return ready to be registered handler
     */

    private Consumer<AbstractDataPackage> audioHandler() {
        return dataPackage -> {
            switch (dataPackage.getHeader().getCode()) {
                case SEND_SOUND: {
                    audioClient.playAudio(dataPackage.getHeader().getFrom(), dataPackage.getData());
                    break;
                }
                case SEND_ADD: {
                    int from = dataPackage.getHeader().getFrom();
                    audioClient.add(from);
                    mainFrame.addToConv(users.get(from).toString(), audioClient.getSettings(from));
                    break;
                }
                case SEND_REMOVE: {
                    audioClient.remove(dataPackage.getHeader().getFrom());
                    mainFrame.removeFromConv(users.get(dataPackage.getHeader().getFrom()).toString());
                    break;
                }
                case SEND_STOP_CONV: {
                    audioClient.close();
                    mainFrame.closeConversation();
                    AbstractDataPackagePool.clearStorage();
                    break;
                }
            }
        };
    }

    /**
     * Creates action for disconnecting or leaving conversation
     * Also clear all existed packages
     *
     * @return ready to use action
     */

    private Runnable endConversation() {
        return () -> {
            audioClient.close();
            controller.getWriter().writeDisconnectFromConv(controller.getMe().getId());
            AbstractDataPackagePool.clearStorage();
        };
    }

    /**
     * Creates action for muting mic on conversation pane
     *
     * @return state of the mic after swap
     */

    private Supplier<Boolean> muteAction() {
        return audioClient::mute;
    }

    @Override
    public void errorCase() {
        users.clear();
        AbstractDataPackagePool.clearStorage();
        iterate();
    }

    @Override
    public ErrorHandler[] getNext() {
        return new ErrorHandler[]{controller, audioClient, callDialog, mainFrame};
    }

}
