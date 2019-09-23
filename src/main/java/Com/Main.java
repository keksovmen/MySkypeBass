package Com;

import Com.GUI.Frame;
import Com.Model.ClientModel;
import Com.Networking.Protocol.AbstractDataPackagePool;
import Com.Networking.Protocol.CODE;
import Com.Networking.Protocol.DataPackagePool;
import Com.Networking.Utility.WHO;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

//
//import Com.Audio.AudioClient;
//import Com.GUI.ActionsBox;
//import Com.GUI.Forms.MainFrame;
//import Com.Model.ClientModel;
//import Com.Networking.ClientController;
//import Com.Networking.Protocol.AbstractDataPackage;
//import Com.Networking.Protocol.AbstractDataPackagePool;
//import Com.Networking.Protocol.CODE;
//import Com.Networking.Protocol.DataPackagePool;
//import Com.Networking.Server;
//import Com.Networking.Utility.BaseUser;
//import Com.Networking.Utility.Call;
//import Com.Networking.Utility.ErrorHandler;
//import Com.Networking.Utility.WHO;
//
//import java.awt.*;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.function.Supplier;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * My interpretation of the whole system
// * Sure you can do better
// */
//



public class Main {

    private static Client client;
    private static Frame frame;


    public static void main(String[] args) {
        CODE.uniqueIdCheck();
        WHO.uniqueIdCheck();
        AbstractDataPackagePool.init(new DataPackagePool());
        client = new Client();
        try {
            SwingUtilities.invokeAndWait(() -> frame = new Frame());
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Swing fucked up in thread invocation");
        }
//        client.init(frame);

        client.registerListener(frame);

        frame.registerListener(client);
    }
}
//
//
//    private final ActionsBox actionsBox;
//    private final ClientController controller;
//    private final Map<Integer, BaseUser> users;
//    private final AudioClient audioClient;
//    private final Call callDialog;
//    private MainFrame mainFrame;
//    private Server server;
//
//    private Main() {
//        AbstractDataPackagePool.init(new DataPackagePool());
//        Client client = new Client();
//
//        audioClient = AudioClient.getInstance();
//        users = new HashMap<>();
//        callDialog = new Call();
//        actionsBox = new ActionsBox();
//        controller = new ClientController(null, new ClientModel());
//
//        initialInitForActions();
//        EventQueue.invokeLater(() -> {
//            mainFrame = new MainFrame(actionsBox);
////            controller.getProcessor().setListener(HandlerProducer.usersIncome(this));
////            controller.getProcessor().setListener(HandlerProducer.showMessage(this));
////            controller.getProcessor().setListener(HandlerProducer.callHandler(this));
////            controller.getProcessor().setListener(HandlerProducer.audioHandler(this));
//        });
//    }
//
//    public static void main(String[] args) {
////        System.setProperty("java.util.logging.config.file", "src\\main\\resources\\properties\\logging.properties");
////        LogManager.getLogManager().readConfiguration();
//        new Main();
//    }
//
//    /**
//     * Parse incoming message string
//     * Tries to find <$[0-9]+?>
//     * And get those digits for the index
//     *
//     * @param message to parse
//     * @return -1 in case if there is no such thing otherwise appropriate value
//     */
//
//    private static int retrieveMessageMeta(String message) {
//        Pattern pattern = Pattern.compile("<\\$\\d+?>");
//        Matcher matcher = pattern.matcher(message);
//        if (matcher.find()) {
//            String rawData = matcher.group();
//            return Integer.valueOf(rawData.replaceAll("(<\\$)|(>)", ""));
//        }
//        return -1;
//    }
//
//    /**
//     * Default init of all back end actions
//     */
//
//    private void initialInitForActions() {
//        actionsBox.updateConnect(connect());
//        actionsBox.updateCreateServer(createServer());
//        actionsBox.updateNameAndId(nameAndId());
//        actionsBox.updateDisconnect(disconnect());
//        actionsBox.updateCallForUsers(callForUsers());
//        actionsBox.updateSendMessage(sendMessage());
//        actionsBox.updateCallSomeOne(callSomeOne());
//        actionsBox.updateCancelCall(cancelCall(true));
//        actionsBox.updateAcceptCall(acceptCall());
//        actionsBox.updateDenyCall(denyCall());
//        actionsBox.updateMute(muteAction());
//        actionsBox.updateEndCall(endConversation());
//        actionsBox.updateChangeMultiplier(audioClient.changeMultiplier());
//        actionsBox.updateSendMessageToConference(sendMessageToConference());
//
//    }
//
//    /**
//     * Tries to connect uses on First skin
//     * String[0] name
//     * String[1] host name / ip
//     * String[2] port
//     * <p>
//     * return true or false in case of success or failure
//     * and null in case of exception
//     *
//     * @return ready to call function
//     */
//
//    private Function<String[], Boolean> connect() {
//        return strings -> {
////            boolean connect = controller.connect(strings[0], strings[1], strings[2]);
//            boolean connect = true;
//            if (connect) {
//                boolean speaker = audioClient.isSpeaker();
//                boolean mic = audioClient.isMic();
//                if (!(speaker && mic)) {
//                    String s = "";
//                    if (!speaker) {
//                        s += "Speaker can't read the format\n";
//                    }
//                    if (!mic) {
//                        s += "Mic can't capture the format\n";
//                    }
//                    s += audioClient.getAudioFormat().toString();
//                    mainFrame.showDialog(s);
//                }
//            }
//            return connect;
//        };
//    }
//
//    /**
//     * Tries to create server on First skin
//     * String[0] port
//     * String[1] sample rate
//     * String[2] sample size
//     * <p>
//     * You can't create new server if you already have one must restart the app
//     *
//     * @return ready yo use action
//     * true if created false otherwise and null if port already in use by other app
//     */
//
//    private Function<String[], Boolean> createServer() {
//        return strings -> {
//            try {
//                if (server == null) {
////                    server = Server.getFromStrings(strings[0], strings[1], strings[2], "");
//                    server.start("Server");
//                    throw new IOException();
////                    return true;
//                }
//                return false;
//            } catch (IOException e) {
//                e.printStackTrace();
//                server = null;
//                return null;
//            }
//        };
//    }
//
//    /**
//     * Uses to indicate your name and id
//     * when Second skin is created
//     * Obtain id only from the server
//     *
//     * @return ready to use name and id provider
//     */
//
//    private Supplier<String> nameAndId() {
////        return () -> controller.getMe().toString();
//        return null;
//    }
//
//    /**
//     * Actions for the disconnect button on second skin
//     *
//     * @return ready to use action
//     */
//
//    private Runnable disconnect() {
//        return () -> {
////            controller.disconnect();
//            audioClient.close();
//
//        };
//    }
//
//    /**
//     * Send call for users
//     *
//     * @return ready to call function
//     */
//
//    private Runnable callForUsers() {
//        return () -> {
////            try {
//////                controller.getWriter().writeUsersRequest(controller.getMe().getId());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//        };
//    }
//
//    /**
//     * Creates a function that sendSound a message to someone
//     *
//     * @return ready to use function
//     */
//
//    private BiConsumer<Integer, String> sendMessage() {
//        return (integer, s) -> {
////            try {
////                controller.getWriter().writeMessage(controller.getMe().getId(), integer, s);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//        };
//    }
//
//    /**
//     * Create a ready to use function
//     * when you need to call some one
//     *
//     * @return ready to use
//     */
//
//    private Consumer<BaseUser> callSomeOne() {
//        return baseUser -> {
//            callDialog.outComingCall();
//            callDialog.setReceiver(baseUser);
////            try {
////                controller.getWriter().writeCall(controller.getMe().getId(), baseUser.getId());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//        };
//    }
//
//    /**
//     * Creates ready to use action for cancelling an incoming call
//     *
//     * @param isYou needs to separate auto cancel and hand cancel
//     * @return ready to use function
//     */
//
//    private Consumer<BaseUser> cancelCall(boolean isYou) {
//        return baseUser -> {
////            try {
////                controller.getWriter().writeCancel(controller.getMe().getId(), baseUser.getId());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//            if (isYou) {
//                callDialog.stopCall();
//            }
//        };
//    }
//
//    /**
//     * Creates ready to use function for accept incoming call
//     * BaseUser[0] = is who calls you
//     * others are who in conversation with him
//     *
//     * @return ready to use function
//     */
//
//    private Consumer<BaseUser[]> acceptCall() {
//        return baseUsers -> {
//            int from = baseUsers[0].getId();
//
//            startConv(baseUsers);
//
////            try {
////                controller.getWriter().writeAccept(controller.getMe().getId(), from);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//
//            callDialog.stopCall();
//        };
//    }
//
//    /**
//     * Creates ready to use action for deny call
//     *
//     * @return ready to use
//     */
//
//    private Consumer<BaseUser> denyCall() {
//        return baseUser -> {
////            try {
////                controller.getWriter().writeDeny(controller.getMe().getId(), baseUser.getId());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//            callDialog.stopCall();
//        };
//    }
//
//    /**
//     * Start conversation
//     * firstly check if there people in incoming conf
//     * than open sound source lines
//     * and at the end display it all n gui
//     *
//     * @param allUsers incoming users from the call can be only 1 person
//     *                 who called you
//     */
//
//    private void startConv(BaseUser[] allUsers) {
//        int from = allUsers[0].getId();
//        if (allUsers.length > 1) {
//            int[] indexes = new int[allUsers.length + 1];
//            for (int i = 0; i < allUsers.length; i++) {
//                indexes[i] = allUsers[i].getId();
//            }
//            indexes[indexes.length - 1] = from;
//            audioClient.startConversation(sendSoundAction(), indexes);
//            mainFrame.startConversation(users.get(from).toString(), audioClient.getSettings(from));
//            //add others on GUI, call after first loop because they don't have controls for audio
//            for (int i = 1; i < allUsers.length; i++) {
//                mainFrame.addToConv(allUsers[i].toString(), audioClient.getSettings(allUsers[i].getId()));
//            }
//        } else {
//            audioClient.startConversation(sendSoundAction(), from);
//            mainFrame.startConversation(allUsers[0].toString(), audioClient.getSettings(from));
//        }
//    }
//
//    /**
//     * Prepare string full of BaseUser.toString()+'\n'
//     * convert in to appropriate array
//     *
//     * @param from      who calls
//     * @param convUsers the boys in conversation
//     */
//
//    private void startConv(int from, String convUsers) {
//        BaseUser caller = users.get(from);
//        BaseUser[] result;
//        if (convUsers.length() > 0) {
//            BaseUser[] parseUsers = BaseUser.parseUsers(convUsers);
//            result = new BaseUser[parseUsers.length + 1];
//            result[0] = caller;
//            System.arraycopy(parseUsers, 0, result, 1, parseUsers.length);
//        } else {
//            result = new BaseUser[]{caller};
//        }
//        startConv(result);
//    }
//
//    /**
//     * Retrieve info for startConv(int, String) method
//     * Server will add conversation info if so exists
//     *
//     * @param baseDataPackage incoming package
//     */
//
//    private void startConv(AbstractDataPackage baseDataPackage) {
//        startConv(baseDataPackage.getHeader().getFrom(), baseDataPackage.getDataAsString());
//    }
//
//    /**
//     * Uses only for AudioCapture class
//     *
//     * @return action for writing sound data
//     */
//
//    private Consumer<byte[]> sendSoundAction() {
//        return bytes -> {
////            try {
////                controller.getWriter().writeSound(controller.getMe().getId(), bytes);
////            } catch (IOException e) {
////                e.printStackTrace();
////                //simple ignore it base reader will handle connection failure
////            }
//        };
//    }
//
//    /**
//     * Creates action for disconnecting or leaving conversation
//     * Also clear all existed packages
//     *
//     * @return ready to use action
//     */
//
//    private Runnable endConversation() {
//        return () -> {
//            audioClient.close();
////            try {
////                controller.getWriter().writeDisconnectFromConv(controller.getMe().getId());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//            AbstractDataPackagePool.clearStorage();
//        };
//    }
//
//    /**
//     * Creates action for muting mic on conversation pane
//     *
//     * @return state of the mic after swap
//     */
//
//    private Supplier<Boolean> muteAction() {
//        return audioClient::mute;
//    }
//
//    /**
//     * Send a message to your conference
//     *
//     * @return ready to use action
//     */
//
//    private Consumer<String> sendMessageToConference() {
//        return s -> {
////            try {
////                controller.getWriter().writeMessage(controller.getMe().getId(), WHO.CONFERENCE.getCode(), s);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//        };
//    }
//
//    @Override
//    public void errorCase() {
//        users.clear();
//        AbstractDataPackagePool.clearStorage();
//        iterate();
//    }
//
//    @Override
//    public ErrorHandler[] getNext() {
//        return new ErrorHandler[]{audioClient, callDialog, mainFrame};
//    }
//
//    private static class HandlerProducer{
//        /**
//         * Creates a handler for users income
//         * gets users from string
//         *
//         * @return ready to be established handler
//         */
//
//        private static Consumer<AbstractDataPackage> usersIncome(final Main main) {
//            return baseDataPackage -> {
//                if (baseDataPackage.getHeader().getCode().equals(CODE.SEND_USERS)) {
//                    BaseUser[] baseUsers = BaseUser.parseUsers(baseDataPackage.getDataAsString());
//                    main.users.clear();
//                    Arrays.stream(baseUsers).forEach(baseUser -> main.users.put(baseUser.getId(), baseUser));
//                    main.mainFrame.updateUsers(baseUsers);
//                }
//            };
//        }
//
//        /**
//         * Creates message handler
//         * displays it
//         *
//         * @return ready to be registered handler
//         */
//
//        private static Consumer<AbstractDataPackage> showMessage(final Main main) {
//            return baseDataPackage -> {
//                if (baseDataPackage.getHeader().getCode().equals(CODE.SEND_MESSAGE)) {
//
//                    String message = baseDataPackage.getDataAsString();
//                    int index = retrieveMessageMeta(message);
//                    if (index != -1) {
//                        main.audioClient.playIndexedMessageSound(index);
//                    } else {
//                        main.audioClient.playRandomMessageSound();
//                    }
//
//                    if (baseDataPackage.getHeader().getTo() == WHO.CONFERENCE.getCode()) {
//                        main.mainFrame.showConferenceMessage(message, main.users.get(baseDataPackage.getHeader().getFrom()).toString());
//                    } else {
//                        main.mainFrame.showMessage(main.users.get(baseDataPackage.getHeader().getFrom()), message);
//                    }
//                }
//            };
//        }
//
//        /**
//         * Creates a call handler
//         * Reacts to all call actions
//         * If you are already in calling then according to
//         * who you try to call it will auto accept or cancel
//         * the incoming call
//         * Starts a conversation if accepted
//         * other cases just dispose the dialog with a message
//         *
//         * @return ready to be registered handler
//         */
//
//        private static Consumer<AbstractDataPackage> callHandler(final Main main) {
//            return baseDataPackage -> {
//                switch (baseDataPackage.getHeader().getCode()) {
//                    case SEND_CALL: {
//                        int from = baseDataPackage.getHeader().getFrom();
//                        if (main.callDialog.isCalling()) {
//                            //auto accept and cancel
//                            BaseUser receiver = main.callDialog.getReceiver();
//                            if (receiver.getId() == from) {
//                                BaseUser[] users;
//                                if (baseDataPackage.getHeader().getLength() > 0) {
//                                    BaseUser[] parseUsers = BaseUser.parseUsers(baseDataPackage.getDataAsString());
//                                    users = new BaseUser[parseUsers.length + 1];
//                                    users[0] = receiver;
//                                    System.arraycopy(parseUsers, 0, users, 1, parseUsers.length);
//                                } else {
//                                    users = new BaseUser[]{receiver};
//                                }
//                                main.actionsBox.acceptCall().accept(users);
//                                //auto accept call handle here
////                            mainFrame.closeCall("Call was accepted");
//                                main.mainFrame.showDialog("Call was accepted");
//                            } else {
//                                BaseUser caller = main.users.get(from);
//                                //auto cancel call because you are busied already
//                                main.cancelCall(false).accept(caller);   //not from actionBox because of boolean argument
//                                main.mainFrame.showMessage(caller, "I called you, but you were busy, call me");
//                            }
//                        } else {
//                            //ordinary 1 side call
//                            main.callDialog.incomingCall();
//                            main.callDialog.setReceiver(main.users.get(baseDataPackage.getHeader().getFrom()));
//                            main.mainFrame.showIncomingCall(main.users.get(baseDataPackage.getHeader().getFrom()).toString(), baseDataPackage.getDataAsString());
//                        }
//                        break;
//                    }
//                    case SEND_CANCEL: {
//                        if (main.callDialog.isCalling() && baseDataPackage.getHeader().getFrom() == main.callDialog.getReceiver().getId()) {
//                            main.mainFrame.closeCall("Call was canceled");
//                            main.callDialog.stopCall();
//                        }
//                        break;
//                    }
//                    case SEND_APPROVE: {
//                        if (main.callDialog.isCalling() && baseDataPackage.getHeader().getFrom() == main.callDialog.getReceiver().getId()) {
//                            main.mainFrame.closeCall("Call was accepted");
//                            main.callDialog.stopCall();
//                            main.startConv(baseDataPackage);
//                        }
//                        break;
//                    }
//                    case SEND_DENY: {
//                        if (main.callDialog.isCalling() && baseDataPackage.getHeader().getFrom() == main.callDialog.getReceiver().getId()) {
//                            main.mainFrame.closeCall("Call was denied");
//                            main.callDialog.stopCall();
//                        }
//                        break;
//                    }
//                }
//            };
//        }
//
//        /**
//         * Create audio handler
//         * Handles all events corresponding to sound
//         * It adds new sound lines and remove already existed
//         * and stop a conversation
//         * <p>
//         * Also clears all existed packages
//         *
//         * @return ready to be registered handler
//         */
//
//        private static Consumer<AbstractDataPackage> audioHandler(final Main main) {
//            return dataPackage -> {
//                switch (dataPackage.getHeader().getCode()) {
//                    case SEND_SOUND: {
//                        main.audioClient.playAudio(dataPackage.getHeader().getFrom(), dataPackage.getData());
//                        break;
//                    }
//                    case SEND_ADD: {
//                        int from = dataPackage.getHeader().getFrom();
//                        main.audioClient.add(from);
//                        main.mainFrame.addToConv(main.users.get(from).toString(), main.audioClient.getSettings(from));
//                        break;
//                    }
//                    case SEND_REMOVE: {
//                        main.audioClient.remove(dataPackage.getHeader().getFrom());
//                        main.mainFrame.removeFromConv(main.users.get(dataPackage.getHeader().getFrom()).toString());
//                        break;
//                    }
//                    case SEND_STOP_CONV: {
//                        main.audioClient.close();
//                        main.mainFrame.closeConversation();
//                        AbstractDataPackagePool.clearStorage();
//                        break;
//                    }
//                }
//            };
//        }
//    }
//
//}
