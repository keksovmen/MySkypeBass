package com.Abstraction.Networking.Servers;

import com.Abstraction.Audio.Misc.AbstractAudioFormatWithMic;
import com.Abstraction.Networking.Handlers.ServerHandler;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.ProtocolBitMap;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Readers.Reader;
import com.Abstraction.Networking.Readers.UDPReader;
import com.Abstraction.Networking.Utility.Authenticator;
import com.Abstraction.Networking.Utility.Conversation;
import com.Abstraction.Networking.Utility.ProtocolValueException;
import com.Abstraction.Networking.Utility.Users.*;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Networking.Writers.PlainWriter;
import com.Abstraction.Networking.Writers.ServerCipherWriter;
import com.Abstraction.Networking.Writers.ServerWriter;
import com.Abstraction.Networking.Writers.Writer;
import com.Abstraction.Util.Algorithms;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Interfaces.Starting;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static com.Abstraction.Util.Logging.LoggerUtils.serverLogger;

/**
 * Implementation of server with my protocol
 * Basic TCP, but best way is made this was use TCP for everything
 * except sound data for that should be better UDP
 * but I am too lazy to implement it
 */

public class SimpleServer extends AbstractServer {


    /**
     * Place where you get your unique id
     * Must starts from WHO. last index + 1
     */

    private final AtomicInteger id;

    /**
     * Where me and the boys are flexing
     */

    private final ConcurrentHashMap<Integer, ServerUser> users;

    /**
     * Format for audio data, if client can't handleDataPackageRouting it on mic or speaker
     * he must be disconnected, but i will rewrite this
     */

    private final AbstractAudioFormatWithMic audioFormat;

    /**
     * Creates server with give parameters
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException            if port already in use
     * @throws ProtocolValueException if mic capture size is grater than possible length of the protocol
     */

    protected SimpleServer(int port, boolean isCipher, Authenticator authenticator, int sampleRate, int sampleSizeInBits, boolean isFullTCP)
            throws IOException, ProtocolValueException {
        super(port, isCipher, authenticator, isFullTCP);
        final int micCapSize;
        try {
            micCapSize = calculateMicCaptureSize(sampleRate, sampleSizeInBits);
        } catch (ProtocolValueException e) {
            serverSocket.close();
            throw e;
        }
        id = new AtomicInteger(WHO.SIZE);//because some ids already in use @see BaseWriter enum WHO
        users = new ConcurrentHashMap<>();//change to one of concurrent maps
        audioFormat = new AbstractAudioFormatWithMic(sampleRate, sampleSizeInBits,
                micCapSize);
    }

    /**
     * Creates server from integers
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public static SimpleServer getFromIntegers(final int port, final int sampleRate, final int sampleSizeInBits, boolean isCipher, Authenticator authenticator, boolean isFullTCP)
            throws IOException, ProtocolValueException {
        return new SimpleServer(
                port,
                isCipher,
                authenticator,
                sampleRate,
                sampleSizeInBits,
                isFullTCP
        );
    }

    /**
     * Creates server from strings
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public static SimpleServer getFromStrings(final String port, final String sampleRate, final String sampleSizeInBits, boolean isCipher, Authenticator authenticator, boolean isFullTCP)
            throws IOException, ProtocolValueException {
        return new SimpleServer(
                Integer.valueOf(port),
                isCipher,
                authenticator,
                Integer.valueOf(sampleRate),
                Integer.parseInt(sampleSizeInBits),
                isFullTCP
        );
    }


    /**
     * Put new user
     * And notifies others with new dude on
     *
     * @param user to add
     */

    @Override
    public void registerUser(ServerUser user) {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "registerUser",
                "User is registered - " + user);
        users.put(user.getId(), user);
        sendAddDude(user);
    }

    /**
     * Remove a user from server
     * Send notification about it
     * Clears any existed data packages in the pool
     *
     * @param user_id of user to remove
     */

    @Override
    public void removeUser(int user_id) {
        users.remove(user_id);
        sendRemoveDude(user_id);
        AbstractDataPackagePool.clearStorage();
    }

    /**
     * Format for transferring audio data
     * and mic capture size for not violating protocol length
     *
     * @return data enough to represent audio format for client
     */

    @Override
    public String getAudioFormat() {
        return AbstractAudioFormatWithMic.intoString(audioFormat);
    }

    /**
     * Method for obtaining all except you users
     * on this Server as string
     *
     * @param exclusiveId you
     * @return all others users
     */

    @Override
    public String getUsersExceptYou(final int exclusiveId) {
        StringBuilder stringBuilder = new StringBuilder(100);
        users.forEach((integer, user) -> {
            if (integer != exclusiveId) {
                stringBuilder.append(user.toNetworkFormat()).append("\n");
            }
        });
        return stringBuilder.toString();
    }

    /**
     * Get packageRouter for other usages
     *
     * @param who to get id
     * @return null if there is no such dude
     */

    @Override
    public ServerUser getUser(int who) {
        return users.get(who);
    }

    @Override
    protected ExecutorService createService() {
        return new ThreadPoolExecutor(
                0,
                8,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "Server helper pool"));
    }

    @Override
    protected void acceptSocket(Socket socket) {
        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException ignored) {
            //fuck him
            Algorithms.closeSocketThatCouldBeClosed(socket);
            return;
        }
        Authenticator.CommonStorage storage = authenticator.serverAuthentication(
                inputStream,
                outputStream,
                getAudioFormat(),
                getIdAndIncrement(),
                isCipherMode,
                isFullTCP,
                FormatWorker.getPackageSizeUDP(isCipherMode, audioFormat.getMicCaptureSize())
        );

        if (storage.isNetworkFailure) {
            Algorithms.closeSocketThatCouldBeClosed(socket);
            return;
        }
        if (!storage.isAudioFormatAccepted) {
            Algorithms.closeSocketThatCouldBeClosed(socket);
            return;
        }
        if (storage.isSecureConnection) {
            if (!storage.isSecureConnectionAccepted) {
                Algorithms.closeSocketThatCouldBeClosed(socket);
                return;
            }
        }
        ServerUser user = createUser(storage, inputStream, outputStream, isFullTCP ? null : socket.getInetAddress());
        registerUser(user);


        ServerHandler serverHandler = createServerHandler(socket, user);
        serverHandler.start("Server - " + user.toString());//already started if true returned
    }

    @Override
    protected ServerHandler createServerHandler(Socket socket, ServerUser user) {
        return new ServerHandler(this, socket, user);
    }

    @Override
    protected Writer createWriterForUser(Authenticator.CommonStorage storage, OutputStream outputStream) {
        Writer writer = new PlainWriter(outputStream, Resources.getInstance().getBufferSize(), serverSocketUDP);
        if (storage.isSecureConnection) {
            return new ServerCipherWriter(writer, storage.cryptoHelper.getKey(), storage.cryptoHelper.getParameters());
        } else {
            return writer;
        }
    }

    @Override
    protected ServerUser createUser(Authenticator.CommonStorage storage, InputStream inputStream, OutputStream outputStream, InetAddress address) {
        final ServerWriter writer = new ServerWriter(createWriterForUser(storage, outputStream));
        final Reader reader = new BaseReader(inputStream, Resources.getInstance().getBufferSize());
        final User user;
        if (storage.isSecureConnection) {
            user = new CipherUser(
                    storage.name,
                    storage.myID,
                    storage.cryptoHelper.getKey(),
                    storage.cryptoHelper.getParameters()
            );
        } else {
            user = new PlainUser(storage.name, storage.myID);
        }
        return new ServerUser(new BaseUserWithLock(user), writer, reader, address, storage.portUDP);
    }

    @Override
    protected void workLoopUDP() {
        UDPReader udpReader = new UDPReader(serverSocketUDP, FormatWorker.getPackageSizeUDP(isCipherMode, audioFormat.getMicCaptureSize()));
        while (isWorking) {
            try {
                AbstractDataPackage dataPackage = udpReader.read();
                final int from = dataPackage.getHeader().getFrom();
                ServerUser user = getUser(from);
                if (user != null) {
                    Conversation conversation = user.getConversation();
                    if (conversation != null) {
                        conversation.sendSound(dataPackage, from);
                    }
                }
                AbstractDataPackagePool.returnPackage(dataPackage);

            } catch (IOException ignored) {
                //TCP socket will handle closing
                break;
            }
        }

    }

    private int calculateMicCaptureSize(int sampleRate, int sampleSizeInBits) throws ProtocolValueException {
        int i = (sampleRate / Resources.getInstance().getMiCaptureSizeDivider()) * (sampleSizeInBits / 8);
        i = i - i % (sampleSizeInBits / 8);
        int udpPackage = FormatWorker.getPackageSizeUDP(isCipherMode, i);
        if (ProtocolBitMap.MAX_VALUE < i || ProtocolBitMap.MAX_VALUE < udpPackage)
            throw new ProtocolValueException("Audio capture size is larger than length of the protocol! " +
                    i + " must be " + "<= " + ProtocolBitMap.MAX_VALUE);
        return i;
    }

    /**
     * @return unique id for a user
     */

    private int getIdAndIncrement() {
        int i = id.getAndIncrement();
        if (ProtocolBitMap.MAX_VALUE < i) {
            System.err.println("Max limit of IDs is exceeded! System shutting down!");
            System.exit(1);
        }
        return i;
    }

    /**
     * Update each user with new users
     *
     * @param userToAdd who to notifyObservers about
     */

    protected void sendAddDude(ServerUser userToAdd) {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "sendAddDude",
                "All others dudes are notified about this user - " + userToAdd);
        users.forEach((integer, user) ->
                {
                    if (integer == userToAdd.getId())
                        return;
                    executorService.execute(() ->
                    {
                        try {
                            user.getWriter().writeAddToUserList(
                                    user.getId(),
                                    userToAdd.toNetworkFormat()
                            );
                        } catch (IOException ignored) {
                            //If exception with io, it must be handled by corresponding thread not yours
                        }
                    });
                }
        );
    }

    /**
     * Send notification to each user that some dude disconnected
     *
     * @param dudesId of disconnected
     */

    protected void sendRemoveDude(int dudesId) {
        users.forEach((integer, user) ->
                executorService.execute(() ->
                {
                    try {
                        user.getWriter().writeRemoveFromUserList(
                                user.getId(),
                                dudesId
                        );
                    } catch (IOException ignored) {
                        //If exception with io, it must be handled by corresponding thread not yours
                    }
                })
        );
    }
}
