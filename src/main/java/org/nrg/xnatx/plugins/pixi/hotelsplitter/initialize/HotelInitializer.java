package org.nrg.xnatx.plugins.pixi.hotelsplitter.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.om.PixiHotel;
import org.nrg.xdat.om.PixiHotelposition;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.exceptions.PixiRuntimeException;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.services.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * HotelInitializer generates a default set of 'mouse' hotels at startup. These hotels
 * are needed for splitting hotel image sessions and some standard hotels are included
 * in the default install of the PIXI plugin. These hotels cover all the mouse hotels
 * in our sample data collection.
 *
 * Hotels:
 * 4 Mouse - [m][m]
 *           [m][m]
 *
 * 2 Mouse - [m][m]
 * 3 Mouse One Top Two Bottom - [  m  ]
 *                              [m] [m]
 * 3 Mouse One Row - [m][m][m]
 *
 * @author Andy Lassiter
 */
@Component
@Slf4j
public class HotelInitializer extends AbstractInitializingTask {

    private final HotelService hotelService;
    private final DatabaseHelper databaseHelper;
    private final JdbcTemplate template;
    private static final String QUERY_PIXI_HOTEL_HISTORY = "SELECT EXISTS(SELECT 1 FROM pixi_hotel_history)";

    @Autowired
    public HotelInitializer(final HotelService hotelService, final JdbcTemplate template ) {
        this.hotelService = hotelService;
        this.databaseHelper = new DatabaseHelper(template);
        this.template = template;
    }

    @Override
    public String getTaskName() {
        return "HotelInitialization";
    }

    /**
     * Main method which initializes all mouse hotels. XNAT is responsible for calling this at startup.
     * @throws InitializingTaskException When database has not been initialized.
     */
    @Override
    protected void callImpl() throws InitializingTaskException {
        log.info("Initializing hotels.");

        try {
            if (!databaseHelper.tableExists("pixi_hotel") || !XFTManager.isInitialized()) {
                log.info("XFT not initialized, deferring execution.");
                throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "The table 'pixi_hotel' does not yet exist. Deferring execution.");
            }
        } catch (SQLException e) {
            String message = "An error occurred trying to access the database to check for the table 'pixi_hotel'.";
            log.error(message, e);
            throw new InitializingTaskException(InitializingTaskException.Level.Error, message, e);
        }

        // See if anything is in the history table, if so the hotels should already have been initialized.
        if (hasNoHistory()) {
            init4MouseHotel();
            init2MouseHotel();
            init3MouseHotel();
            init3MouseOneRowHotel();
            init5MouseHotel();
        }
    }

    /**
     * Initializes the 4 Mouse hotel with two mice on the top row and two on the bottom row.
     */
    private void init4MouseHotel() {
        String hotelName = "4 Mouse";

        log.info("Initializing hotel {}", hotelName);

        if (hotelExists(hotelName)) {
            log.info("{} hotel already exists. Skipping initialization", hotelName);
            return;
        }

        PixiHotel hotel = newHotel(hotelName);

        PixiHotelposition hotelPosition1 = newHotelPosition("Top Left",    1, 1);
        PixiHotelposition hotelPosition2 = newHotelPosition("Top Right",   2, 1);
        PixiHotelposition hotelPosition3 = newHotelPosition("Bottom Left", 1, 2);
        PixiHotelposition hotelPosition4 = newHotelPosition("Bottom Right",2, 2);

        addHotelPositions(hotel, hotelPosition1, hotelPosition2, hotelPosition3, hotelPosition4);

        saveHotel(hotel);

        log.info("{} hotel initialized", hotelName);
    }

    /**
     * Initializes the 2 Mouse hotel with two mice in one row.
     */
    private void init2MouseHotel() {
        String hotelName = "2 Mouse";

        log.info("Initializing hotel {}", hotelName);

        if (hotelExists(hotelName)) {
            log.info("{} hotel already exists. Skipping initialization", hotelName);
            return;
        }

        PixiHotel hotel = newHotel(hotelName);

        PixiHotelposition hotelPosition1 = newHotelPosition("Left", 1, 1);
        PixiHotelposition hotelPosition2 = newHotelPosition("Right",2, 1);

        addHotelPositions(hotel, hotelPosition1, hotelPosition2);

        saveHotel(hotel);

        log.info("{} hotel initialized", hotelName);
    }

    /**
     *  Initializes a 3 Mouse hotel with one mouse on the top row and two on the bottom row.
     */
    private void init3MouseHotel() {
        String hotelName = "3 Mouse - One Top, Two Bottom";

        log.info("Initializing hotel {}", hotelName);

        if (hotelExists(hotelName)) {
            log.info("{} hotel already exists. Skipping initialization", hotelName);
            return;
        }

        PixiHotel hotel = newHotel(hotelName);

        PixiHotelposition hotelPosition1 = newHotelPosition("Top",         1, 1);
        PixiHotelposition hotelPosition2 = newHotelPosition("Bottom Left", 1, 2);
        PixiHotelposition hotelPosition3 = newHotelPosition("Bottom Right",2, 2);

        addHotelPositions(hotel, hotelPosition1, hotelPosition2, hotelPosition3);

        saveHotel(hotel);

        log.info("{} hotel initialized", hotelName);
    }

    /**
     * Initializes a 3 Mouse hotel with all mice in one row.
     */
    private void init3MouseOneRowHotel() {
        String hotelName = "3 Mouse - One Row";

        log.info("Initializing hotel {}", hotelName);

        if (hotelExists(hotelName)) {
            log.info("{} hotel already exists. Skipping initialization", hotelName);
            return;
        }

        PixiHotel hotel = newHotel(hotelName);

        PixiHotelposition hotelPosition1 = newHotelPosition("Left",   1, 1);
        PixiHotelposition hotelPosition2 = newHotelPosition("Middle", 2, 1);
        PixiHotelposition hotelPosition3 = newHotelPosition("Right",  3, 1);

        addHotelPositions(hotel, hotelPosition1, hotelPosition2, hotelPosition3);

        saveHotel(hotel);

        log.info("{} hotel initialized", hotelName);
    }

    /**
     * Initializes a 5 Mouse hotel with five mice in one row.
     */
    private void init5MouseHotel() {
        String hotelName = "5 Mouse";

        log.info("Initializing hotel {}", hotelName);

        if (hotelExists(hotelName)) {
            log.info("{} hotel already exists. Skipping initialization", hotelName);
            return;
        }

        PixiHotel hotel = newHotel(hotelName);

        PixiHotelposition hotelPosition1 = newHotelPosition("Position1", 1, 1);
        PixiHotelposition hotelPosition2 = newHotelPosition("Position2", 2, 1);
        PixiHotelposition hotelPosition3 = newHotelPosition("Position3", 3, 1);
        PixiHotelposition hotelPosition4 = newHotelPosition("Position4", 4, 1);
        PixiHotelposition hotelPosition5 = newHotelPosition("Position5", 5, 1);

        addHotelPositions(hotel, hotelPosition1, hotelPosition2, hotelPosition3, hotelPosition4, hotelPosition5);

        saveHotel(hotel);

        log.info("{} hotel initialized", hotelName);
    }

    /**
     * Check to see if the hotel name is already in use.
     * @param hotelName Name of the hotel.
     * @return True if hotel name exists in the database and False if does not exist.
     */
    private boolean hotelExists(final String hotelName) {
        return hotelService.findByName(Users.getAdminUser(), hotelName).isPresent();
    }

    /**
     * Convenience method for generating a new hotel.
     * @param hotelName Name of the new hotel.
     * @return A hotel with the supplied name and hotel positions.
     */
    private PixiHotel newHotel(final String hotelName) {
        PixiHotel hotel = new PixiHotel();
        hotel.setName(hotelName);
        return hotel;
    }

    /**
     * Convenience method for generating a new hotel position.
     * @param positionName A name which describes the hotel position. (examples: left, right, bottom left)
     * @param x This value starts at one for the left most subject holder and monotonically increases by one for each successive subject holder towards the right.
     * @param y This value starts at one for the top most subject holder and monotonically increases by one for each successively lower subject holder.
     * @return A new hotel position with the supplied name, x, y, and z=1
     */
    private PixiHotelposition newHotelPosition(final String positionName, final int x, final int y) {
        PixiHotelposition hotelPosition = new PixiHotelposition();
        hotelPosition.setName(positionName);
        hotelPosition.setX(x);
        hotelPosition.setY(y);
        hotelPosition.setZ(1);

        return hotelPosition;
    }

    /**
     * Convenience method for adding all hotel positions to a hotel.
     * @param hotel The hotel in need of positions.
     * @param hotelPositions The positions to add to the hotel.
     */
    private void addHotelPositions(final PixiHotel hotel, final PixiHotelposition... hotelPositions) {
        Arrays.stream(hotelPositions).forEach(hotelPosition -> {
            try {
                hotel.addPositions_position(hotelPosition);
            } catch (Exception e) {
                log.error("Failed to set hotel positions for hotel " + hotel.getName(), e);
                throw new PixiRuntimeException("Failed to set hotel positions for hotel " + hotel.getName());
            }
        });
    }

    /**
     * Convenience method for saving hotel to the database.
     * @param hotel The hotel to save to the database
     */
    private void saveHotel(final PixiHotel hotel) {
        try {
            hotelService.create(Users.getAdminUser(), hotel);
        } catch (ResourceAlreadyExistsException | DataFormatException e) {
            log.error("Failed to save " + hotel.getName() + " hotel", e);
            throw new PixiRuntimeException("Failed to save " + hotel.getName() + " hotel");
        }
    }

    /**
     * Check to see if the pixi_hotel_history table has any rows. This is used to prevent
     * from reinitializing hotels which names have been changed by the user or hotels that
     * have been deleted.
     * @return True is table has atleast one row, False if empty.
     */
    private boolean hasNoHistory() {
        return !template.queryForObject(QUERY_PIXI_HOTEL_HISTORY, Boolean.class);
    }
}
