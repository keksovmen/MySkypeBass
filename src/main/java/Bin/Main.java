package Bin;

import Bin.Audio.AudioClient;
import Bin.GUI.ActionsBox;
import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Forms.MainFrame;
import Bin.Networking.ClientController;
import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.Server;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.Call;
import Bin.Networking.Writers.BaseWriter;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Main {

    private ActionsBox actionsBox;
    private MainFrame mainFrame;
    private ClientController controller;
    private Server server;
    private final Map<Integer, BaseUser> users;
    private final AudioClient audioClient;
    private final Call callDialog;

    private Main() {
        audioClient = AudioClient.getInstance();
        users = new HashMap<>();
        callDialog = new Call();
        actionsBox = new ActionsBox();
        controller = new ClientController();

        initialInitForActions();
        EventQueue.invokeLater(() -> {
            try {
                mainFrame = new MainFrame(actionsBox);
            } catch (NotInitialisedException e) {
                e.printStackTrace();
            }
            controller.getProcessor().addTaskListener(usersIncome());
            controller.getProcessor().addTaskListener(showMessage());
            controller.getProcessor().addTaskListener(callHandler());
            controller.getProcessor().addTaskListener(audioHandler());
        });
    }

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

    }

    public static void main(String[] args) {
//        Main main = new Main();
        new Main();
//        new Main();

    }

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

    private Function<String[], Boolean> createServer() {
        return strings -> {
            try {
                if (server != null) server.close();
                server = new Server(strings[0], strings[1], strings[2]);
                return server.start();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    private Supplier<String> nameAndId() {
        return () -> controller.getMe().toString();
    }

    private Runnable disconnect() {
        return () -> controller.disconnect();
    }

    private Runnable callForUsers() {
        return () -> {
            try {
                controller.getWriter().writeUsersRequest(controller.getMe().getId());
            } catch (IOException e) {
                e.printStackTrace();
                error();
//                controller.disconnect();
//                mainFrame.errorCase();
            }
        };
    }

    private Consumer<BaseDataPackage> usersIncome() {
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_USERS)) {
                BaseUser[] baseUsers = ClientController.parseUsers(baseDataPackage.getDataAsString());
                users.clear();
                Arrays.stream(baseUsers).forEach(baseUser -> users.put(baseUser.getId(), baseUser));
                mainFrame.updateUsers(baseUsers);
            }
        };
    }

    private BiConsumer<Integer, String> sendMessage() {
        return (integer, s) -> {
            try {
                controller.getWriter().writeMessage(controller.getMe().getId(), integer, s);
            } catch (IOException e) {
                e.printStackTrace();
                error();
            }
        };
    }

    private void error() {
        controller.disconnect();
        mainFrame.errorCase();
        audioClient.close();
    }

    private Consumer<BaseDataPackage> showMessage() {
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_MESSAGE)) {
                mainFrame.showMessage(users.get(baseDataPackage.getHeader().getFrom()), baseDataPackage.getDataAsString());
                audioClient.playMessageSound();
            }
        };
    }

    private Consumer<BaseUser> callSomeOne() {
        return baseUser -> {
            try {
//                if (isCalling.get()) return;//shouldn't happen because of GUI
                controller.getWriter().writeCall(controller.getMe().getId(), baseUser.getId());
                callDialog.setCalling(true, false);
                callDialog.setReceiver(baseUser);
            } catch (IOException e) {
                e.printStackTrace();
                error();
            }
        };
    }

    private Consumer<BaseUser> cancelCall(boolean isYou) {
        return baseUser -> {
            try {
                controller.getWriter().writeCancel(controller.getMe().getId(), baseUser.getId());
                if (isYou)
                    callDialog.setCalling(false);
            } catch (IOException e) {
                e.printStackTrace();
                error();
            }
        };
    }

    private Consumer<BaseUser[]> acceptCall() {
        return baseUsers -> {
            try {
                int from = baseUsers[0].getId();
                callDialog.setCalling(false);
                controller.getWriter().writeAccept(controller.getMe().getId(), from);

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i < baseUsers.length; i++) {
                    stringBuilder.append(baseUsers[i].toString()).append("\n");
                }
                startConv(from, stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
                error();
            }
        };
    }

    private Consumer<BaseUser> denyCall() {
        return baseUser -> {
            try {
                controller.getWriter().writeDeny(controller.getMe().getId(), baseUser.getId());
                callDialog.setCalling(false);
            } catch (IOException e) {
                e.printStackTrace();
                error();
            }
        };
    }

    private Consumer<BaseDataPackage> callHandler() {
        return baseDataPackage -> {
            switch (baseDataPackage.getHeader().getCode()) {
                case SEND_CALL: {
                    int from = baseDataPackage.getHeader().getFrom();
                    //auto accept and cancel
                    if (callDialog.isCalling()) {
                        if (callDialog.getReceiver().getId() == from) {
                            BaseUser[] users = null;
                            if (baseDataPackage.getHeader().getLength() > 0) {
                                BaseUser[] parseUsers = ClientController.parseUsers(baseDataPackage.getDataAsString());
                                users = new BaseUser[parseUsers.length + 1];
                                users[0] = callDialog.getReceiver();
                                System.arraycopy(parseUsers, 0, users, 1, parseUsers.length);
                            } else {
                                users = new BaseUser[]{callDialog.getReceiver()};
                            }
                            acceptCall().accept(users);
                            //auto accept call handle here
                            mainFrame.closeCall("Call was accepted");
                        } else {
                            BaseUser caller = users.get(from);
                            //auto cancel call because you are busied already
                            cancelCall(false).accept(caller);
                            mainFrame.showMessage(caller, "I called you, but you were busy, call me");
                        }
                        break;
                    }
                    //ordinary 1 side call
                    callDialog.setCalling(true, true);
                    callDialog.setReceiver(users.get(baseDataPackage.getHeader().getFrom()));
                    mainFrame.showIncomingCall(users.get(baseDataPackage.getHeader().getFrom()).toString(), baseDataPackage.getDataAsString());
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
//                        audioClient.startConversation(controller.getWriter(), controller.getMe().getId(), baseDataPackage.getHeader().getFrom());
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

    private void startConv(BaseDataPackage baseDataPackage) {
        startConv(baseDataPackage.getHeader().getFrom(), baseDataPackage.getDataAsString());
    }

    private void startConv(int from, String convUsers) {
        if (convUsers.length() > 0) {
            BaseUser[] baseUsers = ClientController.parseUsers(convUsers);
            int[] indexes = new int[baseUsers.length + 1];
            for (int i = 0; i < baseUsers.length; i++) {
                indexes[i] = baseUsers[i].getId();
            }
            indexes[indexes.length - 1] = from;
            audioClient.startConversation(controller.getWriter(), controller.getMe().getId(), indexes);
            mainFrame.startConversation(users.get(from).toString(), audioClient.getSettings(from));
            for (BaseUser baseUser : baseUsers) {
                mainFrame.addToConv(baseUser.toString(), audioClient.getSettings(baseUser.getId()));
            }
        } else {
            audioClient.startConversation(controller.getWriter(), controller.getMe().getId(), from);
            mainFrame.startConversation(users.get(from).toString(), audioClient.getSettings(from));
        }


    }

    private Consumer<BaseDataPackage> audioHandler() {
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
                    break;
                }
            }
        };
    }

    private Runnable endConversation() {
        return () -> {
            try {
                audioClient.close();
                mainFrame.closeConversation();
                controller.getWriter().writeDisconnectFromConv(controller.getMe().getId());
            } catch (IOException e) {
                e.printStackTrace();
                error();
            }
        };
    }

    private Supplier<Boolean> muteAction() {
        return audioClient::mute;
    }


}
