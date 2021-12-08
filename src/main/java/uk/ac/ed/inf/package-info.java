/**
 * This package contains the functions used for the drone to deliver orders that were made by users
 * on the data specified. The orders and locations are accessed from the database and the server
 * respectively. A flightpath is generated for the drone to collect and deliver orders while avoiding
 * the no-fly zone. The flightpath is then written to a GeoJSON file, and it is also written to a new table
 * in the database containing details of the moves made, along with another created table which contains
 * the details of the orders delivered by the drone.
 * @author sarah
 */
package uk.ac.ed.inf;