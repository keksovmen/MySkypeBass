package com.Networking.Servers;

import com.Networking.Handlers.ServerHandler;
import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.CODE;
import com.Networking.Protocol.ProtocolBitMap;
import com.Networking.Readers.BaseReader;
import com.Networking.Utility.ProtocolValueException;
import com.Networking.Utility.Users.ServerUser;
import com.Networking.Utility.WHO;
import com.Networking.Writers.ServerWriter;
import com.Util.FormatWorker;
import com.Util.Resources;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of server with my protocol
 * Basic TCP, but best way is made this was use TCP for everything
 * except sound data for that should be better UDP
 * but I too lazy to implement it
 */

public class SimpleServer extends AbstractServer {


    public final int BUFFER_SIZE_FOR_IO;

    /**
     * Must be less or equal ProtocolBitMap.MAX_VALUE
     */

    private final int MIC_CAPTURE_SIZE;

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
     * Format for audio data, if client can't handleRequest it on mic or speaker
     * he must be disconnected, but i will rewrite this
     */

    private final AudioFormat audioFormat;

    /**
     * Creates server with give parameters
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException            if port already in use
     * @throws ProtocolValueException if mic capture size is grater than possible length of the protocol
     */

    protected SimpleServer(int port, int sampleRate, int sampleSizeInBits)
            throws IOException, ProtocolValueException {
        super(port);
        BUFFER_SIZE_FOR_IO = Resources.getBufferSize() * 1024;
        try {
            MIC_CAPTURE_SIZE = calculateMicCaptureSize(sampleRate, sampleSizeInBits);
        } catch (ProtocolValueException e) {
            serverSocket.close();
            throw e;
        }
        id = new AtomicInteger(WHO.SIZE);//because some ids already in use @see BaseWriter enum WHO
        users = new ConcurrentHashMap<>();//change to one of concurrent maps
        audioFormat = new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                1,
                true,
                true);
    }

    /**
     * Creates server from integers
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public static SimpleServer getFromIntegers(final int port, final int sampleRate, final int sampleSizeInBits)
            throws IOException, ProtocolValueException {
        return new SimpleServer(
                port,
                sampleRate,
                sampleSizeInBits
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

    public static SimpleServer getFromStrings(final String port, final String sampleRate, final String sampleSizeInBits)
            throws IOException, ProtocolValueException {
        return new SimpleServer(
                Integer.valueOf(port),
                Integer.valueOf(sampleRate),
                Integer.parseInt(sampleSizeInBits)
        );
    }


    /**
     * Trying to register a new user for the server
     * first read name from the user
     * second writes audio format
     * third gets true or false on the audio format
     * than add user or disconnect him
     * after write all users on server to him
     * and notifyObservers all other users
     *
     * @param reader used to read packages
     * @param writer used to write to the dude
     * @return true only if you are able to use this audio format
     */

    @Override
    public ServerUser authenticate(BaseReader reader, ServerWriter writer) {
        try {
            AbstractDataPackage dataPackage = reader.read();
            final String name = dataPackage.getDataAsString();
            AbstractDataPackagePool.returnPackage(dataPackage);

            writer.writeAudioFormat(WHO.NO_NAME.getCode(), getAudioFormat());
            dataPackage = reader.read();

            if (dataPackage.getHeader().getCode() != CODE.SEND_APPROVE) {
                //Then dude just disconnects so do we
                AbstractDataPackagePool.returnPackage(dataPackage);
                return null;
            }
            AbstractDataPackagePool.returnPackage(dataPackage);

            final int id = getIdAndIncrement();
            writer.writeId(id);

            return new ServerUser(name, id, writer);

        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Put new user
     * And notifies others with new dude on
     *
     * @param user to add
     */

    @Override
    public void registerUser(ServerUser user) {
        users.put(user.getId(), user);
//        if (work)
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
//        if (work)
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
        return FormatWorker.getFullAudioPackage(audioFormat, MIC_CAPTURE_SIZE);
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
        StringBuilder stringBuilder = new StringBuilder(50);
        users.forEach((integer, user) -> {
            if (integer != exclusiveId) {
                stringBuilder.append(user.toString()).append("\n");
            }
        });
        return stringBuilder.toString();
    }

    /**
     * Get controller for other usages
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
                new LinkedBlockingQueue<>());
    }

    @Override
    protected void acceptSocket(Socket socket) {
        ServerHandler serverHandler = new ServerHandler(this, socket);
        if (!serverHandler.start("SimpleServer controller - ")) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private int calculateMicCaptureSize(int sampleRate, int sampleSizeInBits) throws ProtocolValueException {
        int i = (sampleRate / Resources.getMiCaptureSizeDivider()) * (sampleSizeInBits / 8);
        i = i - i % (sampleSizeInBits / 8);
        if (ProtocolBitMap.MAX_VALUE < i)
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
        users.forEach((integer, user) ->
                {
                    if (integer == userToAdd.getId())
                        return;
                    executorService.execute(() ->
                    {
                        try {
                            user.getWriter().writeAddToUserList(
                                    user.getId(),
                                    userToAdd.toString()
                            );
                        } catch (IOException ignored) {
                            //If exception with io, is must be handled by corresponding thread not yours
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
                        //If exception with io, is must be handled by corresponding thread not yours
                    }
                })
        );
    }
}
