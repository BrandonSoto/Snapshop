/*
 * TCSS 305 Winter 2014
 * Assignment 4 - SnapShop
 */
package snapshop.gui;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import snapshop.filters.EdgeDetectFilter;
import snapshop.filters.EdgeHighlightFilter;
import snapshop.filters.Filter;
import snapshop.filters.FlipHorizontalFilter;
import snapshop.filters.FlipVerticalFilter;
import snapshop.filters.GrayscaleFilter;
import snapshop.filters.SharpenFilter;
import snapshop.filters.SoftenFilter;
import snapshop.image.Pixel;
import snapshop.image.PixelImage;

/**
 * SnapShopGUI is an GUI that displays images and allows a user to manipulate the images. 
 * 
 * @author Brandon Soto
 * @version 2/2/2014
 */
public final class SnapShopGUI {
    
    /**
     * Represents the main frame of the GUI. 
     */
    private final JFrame myMainFrame; 
    
    /**
     * FileChooser to be used by GUI. 
     */
    private final JFileChooser myChooser; 
    
    /**
     * Main label that contains the image as an icon. 
     */
    private final JLabel myImageLabel; 
    
    /**
     * Represents the image to be manipulated. 
     */
    private PixelImage myCurrentImage; 
    
    /**
     * The panel in the middle of the GUI that contains the image. 
     */
    private final JPanel myCenterPanel;
    
    /**
     * List containing all of the buttons that should be enabled and disabled at certain times.
     * (should contain all buttons but the "Open..." and "Undo" buttons. 
     */
    private final List<JButton> myButtonList; 
    
    /**
     * Represents the image before a filter is applied. Used for undo functionality. 
     */
    private Pixel[][] myLastImage; 
    
    /**
     * Button with an undo functionality. 
     */
    private final JButton myUndoButton; 
    
    /**
     * Constructs a SnapShopGUI. 
     */
    public SnapShopGUI() {
        // set up main frame
        myMainFrame = new JFrame("TCSS 305 SnapShop");
        myMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myMainFrame.setLocationByPlatform(true);
        
        // initialize most other fields 
        myImageLabel = new JLabel();
        myCenterPanel = new JPanel(new GridBagLayout());
        myChooser = new JFileChooser(new File("."));
        myButtonList = new ArrayList<JButton>();
        myUndoButton = createUndoButton();
    }
    
    /**
     * Starts the GUI by adding all buttons to the its frame and making the frame visible.
     */
    public void start() {
        // add panels to main frame
        myMainFrame.add(makeTopPanel(), BorderLayout.NORTH);
        myMainFrame.add(makeBottomPanel(), BorderLayout.SOUTH);
        
        myMainFrame.pack();
        myMainFrame.setVisible(true);
    }
    
    /**
     * Enables or disables all of the buttons in the GUI except the "Open..." and undo buttons.
     * 
     * @param theIsEnabled determines whether the buttons will be enabled or disabled.
     */
    private void enableButtons(final boolean theIsEnabled) {
        for (final JButton button : myButtonList) {
            button.setEnabled(theIsEnabled);
        }
    }
    
    
    /**
     * Returns the top panel of the GUI. The top panel contains buttons for all of the 
     * filter buttons. 
     * 
     * @return the top panel to be added to the main frame. 
     */
    private JPanel makeTopPanel() {
        return makePanelWithButtons(createFilterButton(new EdgeDetectFilter()),
                               createFilterButton(new EdgeHighlightFilter()), 
                               createFilterButton(new FlipHorizontalFilter()),
                               createFilterButton(new FlipVerticalFilter()), 
                               createFilterButton(new GrayscaleFilter()),
                               createFilterButton(new SharpenFilter()), 
                               createFilterButton(new SoftenFilter())); 
    }
    
    
    /**
     * Returns the bottom panel of the GUI. The bottom panel contains open, save, undo, and 
     * close buttons. 
     * 
     * @return bottom panel of the GUI. 
     */
    private JPanel makeBottomPanel() {
        final JButton open = new JButton("Open...", new ImageIcon("icons/open.gif"));
        open.setMnemonic('O');
        open.addActionListener(new OpenFileListener());
        
        final JButton save = createSaveButton();
        myButtonList.add(save);
        
        final JButton close = createCloseButton();
        myButtonList.add(close);
        
        return makePanelWithButtons(open, save, myUndoButton, close); 
    }
    
    /**
     * Returns a save button with an action listener that brings up a save dialog. 
     * 
     * @return save button that, when pressed, brings up a save dialog. 
     */
    private JButton createSaveButton() {
        final JButton save = new JButton("Save As...", new ImageIcon("icons/save.gif"));
        save.setEnabled(false);
        save.setMnemonic('S');
        save.addActionListener(new ActionListener() {
            /**
             * When the save button is pressed a save dialog should be shown. If the user 
             * chooses the Approve option, then the file should be saved. 
             * 
             * @param theEvent event when the save button is pressed. 
             */
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                if (myChooser.showSaveDialog(myMainFrame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        myCurrentImage.save(myChooser.getSelectedFile());
                    } catch (final IOException exception) {
                        JOptionPane.showMessageDialog(myMainFrame, exception.getMessage());
                    }
                }
            }
        });
        
        return save; 
    }
    
    /**
     * Returns a close button with an action listener that closes the current image. 
     * 
     * @return close button that closes the image when pressed. 
     */
    private JButton createCloseButton() {
        final JButton close = new JButton("Close Image", new ImageIcon("icons/close.gif"));
        close.setEnabled(false);
        close.setMnemonic('C');
        close.addActionListener(new ActionListener() {
            /**
             * When the close button is pressed, the buttons should be disabled and the image
             * should be removed from the frame. 
             * 
             * @param theEvent event when the close button is pressed. 
             */
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                enableButtons(false);
                
                myUndoButton.setEnabled(false);
                
                myMainFrame.remove(myCenterPanel);
                myMainFrame.pack();
            }
        });
        
        return close; 
    }
    
    /**
     * Returns an undo button with an action listener that can undo the latest change to the 
     * image. 
     * 
     * @return undo button that can undo a change made to the image. 
     */
    private JButton createUndoButton() {
        final JButton undo = new JButton("Undo", new ImageIcon("icons/undo.png"));
        undo.setEnabled(false);
        undo.setMnemonic('U');
        undo.addActionListener(new ActionListener() {
            /**
             * Reverts the image back to the last change made. 
             * 
             * @param theEvent event when the undo button is pushed. 
             */
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                myCurrentImage.setPixelData(myLastImage); 

                myImageLabel.setIcon(new ImageIcon(myCurrentImage));

                undo.setEnabled(false);
            } 
        });
        
        return undo; 
    }
    
    /**
     * Return a button with the given name and an action listener that applies the given 
     * filter to the image. 
     * 
     * @param theFilter filter to be applied to button
     * @return button with a given name and an action listener. 
     */
    private JButton createFilterButton(final Filter theFilter) {
        final JButton button = new JButton(theFilter.getDescription());
        button.setEnabled(false);
        button.addActionListener(new ActionListener() {
            /**
             * When the button is pressed, the button's filter should be applied to the image. 
             * The image at the center of the GUI should then be updated. 
             * 
             * @param theEvent event when the button is pressed.
             */
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                if (myCurrentImage != null) {
                    myLastImage = myCurrentImage.getPixelData(); 
                    myUndoButton.setEnabled(true);

                    theFilter.filter(myCurrentImage);
                    
                    myImageLabel.setIcon(new ImageIcon(myCurrentImage));
                }
            }
        });
        myButtonList.add(button);
        
        return button; 
    }
    
    /**
     * Returns a panel containing all of the buttons passed to this method. 
     * 
     * @param theButtons buttons to be added to panel.
     * @return panel containing all of the buttons.
     */
    private JPanel makePanelWithButtons(final JButton ...theButtons) {
        final JPanel panel = new JPanel();
        
        for (final JButton button : theButtons) {
            panel.add(button);
        }
        
        return panel; 
    }
    
    /**
     * Represents an action listener for an open file dialog. 
     */
    private final class OpenFileListener implements ActionListener {
        /**
         * When the open button is pressed, an open file dialog should be shown. The selected
         * file should be an image file. The image should then be added to the center of the
         * GUI. 
         * 
         * @param theEvent event when the open button has been pressed. 
         */
        @Override
        public void actionPerformed(final ActionEvent theEvent) {
            if (myChooser.showOpenDialog(myMainFrame) == JFileChooser.APPROVE_OPTION) {
                final File imageFile = new File(myChooser.getSelectedFile().getPath());
                
                try {
                    myCurrentImage = PixelImage.load(imageFile);
                    
                    myImageLabel.setIcon(new ImageIcon(myCurrentImage));
                    
                    // make sure to remove old image before adding new one - otherwise the new
                    // image will be added next to the old image. 
                    myCenterPanel.removeAll();
                    myCenterPanel.add(myImageLabel); 
                    
                    enableButtons(true);

                    // add center panel to main frame
                    myMainFrame.add(myCenterPanel, BorderLayout.CENTER);
                    myMainFrame.pack();
                } catch (final IOException exception) {
                    JOptionPane.showMessageDialog(myMainFrame, exception.getMessage());
                }
            }
        }
    } // end OpenFileListener class
    
} // end SnapShop class
