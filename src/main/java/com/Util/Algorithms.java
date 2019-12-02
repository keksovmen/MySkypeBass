package com.Util;

import com.Client.ButtonsHandler;
import com.Client.Logic;
import com.Model.ChangeableModel;
import com.Networking.Utility.Users.BaseUser;
import com.Pipeline.ACTIONS;
import com.Pipeline.BUTTONS;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public class Algorithms {

    private Algorithms() {
    }

    /**
     * Simple linear search for a given value
     *
     * @param array where to search
     * @param item  the value
     * @param <T>   template class
     * @return true if found
     */

    public static <T> boolean search(T[] array, T item) {
        for (T t : array) {
            if (t.equals(item))
                return true;
        }
        return false;
    }

    /**
     * Simple linear search for a given value
     *
     * @param array      where to search
     * @param item       the value
     * @param comparator used to find identical but different classes thing
     * @param <T>        template class for array
     * @param <Y>        template class for finding thing
     * @return T or null
     */

    public static <T, Y> T search(T[] array, Y item, BiFunction<T, Y, Boolean> comparator) {
        for (T t : array) {
            if (comparator.apply(t, item))
                return t;
        }
        return null;
    }

    /**
     * Simple geometric search for identities
     *
     * @param array where to search
     * @param <T>   template class
     * @return true if find identities
     */

    public static <T> boolean searchForIdentities(T[] array) {
        for (int i = 0; i < array.length; i++) {
            T tmp = array[i];
            for (int k = 0; k < array.length; k++) {
                if (i == k)
                    continue;
                if (array[k].equals(tmp))
                    return true;
            }
        }
        return false;
    }

    /**
     * Simple geometric search for identities
     *
     * @param comparator used instead of equals()
     * @param array      where to search
     * @param <T>        template class
     * @return true if find identities
     */

    public static <T> boolean searchForIdentities(T[] array, Comparator<T> comparator) {
        for (int i = 0; i < array.length; i++) {
            T tmp = array[i];
            for (int k = 0; k < array.length; k++) {
                if (i == k)
                    continue;

                if (comparator.compare(array[k], tmp) == 0)
                    return true;
            }
        }
        return false;
    }

    /**
     * Combine two bytes in to an int
     *
     * @param highByte  will be high
     * @param lowerByte will be low
     * @return unsigned int
     */

    public static int combineTwoBytes(byte highByte, byte lowerByte) {
        int result = (highByte & 0xff) << 8;
        result |= lowerByte & 0xff;
        result &= 0xffff;
        return result;
    }

    /**
     * Find value corresponding to percentage
     *
     * @param min        possible value
     * @param max        possible value
     * @param percentage between 0 - 100
     * @return value between min and max equal to the percentage
     */

    public static int findPercentage(int min, int max, int percentage) {
        if (percentage < 0 || 100 < percentage)
            throw new IllegalArgumentException("Percentage can't be more than 100 or less than 0! " + percentage);
        int difference = max - min;
        float percent = difference / 100f;
        float value = percent * percentage;
        return (int) (min + value);
    }

    /**
     * For client side
     *
     * @param dude   who to add in a conversation
     * @param others who may present in the conversation
     * @param logic  what to notifyObservers about progress
     * @param model  to update about progress
     */

    public static void callAcceptRoutine(BaseUser dude, String others, Logic logic, ChangeableModel model) {
        logic.notifyObservers(ACTIONS.CALL_ACCEPTED, null);
        model.addToConversation(dude);
        for (BaseUser baseUser : BaseUser.parseUsers(others)) {
            model.addToConversation(baseUser);
        }
    }

    /**
     * Put pop up menu on a given component
     * That will give you view on all possible sounds and preview them
     *
     * @param component     where to register
     * @param textField     where put meta symbols to send
     * @param buttonsHandler helpHandler to preview sound
     */

    public static void registerPopUp(JComponent component, JTextField textField, ButtonsHandler buttonsHandler) {
        JPopupMenu popupMenu = new JPopupMenu("Sounds");
        List<String> getDescriptions = Resources.getDescriptions();

        for (int i = 0; i < getDescriptions.size(); i++) {
            JMenuItem menuItem = new JMenuItem(getDescriptions.get(i));
            int j = i;
            menuItem.addActionListener(e -> {
                if (e.getModifiers() == InputEvent.META_MASK) {
                    buttonsHandler.handleRequest(BUTTONS.PREVIEW_SOUND, new Object[]{FormatWorker.asMessageMeta(j), j});
                } else {
                    textField.setText(textField.getText() + FormatWorker.asMessageMeta(j));
                }
            });
            popupMenu.add(menuItem);
        }
        component.setComponentPopupMenu(popupMenu);
    }
}
