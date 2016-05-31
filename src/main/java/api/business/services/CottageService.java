package api.business.services;

import api.business.entities.*;
import api.business.persistance.ISimpleEntityManager;
import api.business.services.interfaces.ICottageService;
import api.business.services.interfaces.IPaymentsService;
import api.business.services.interfaces.IUserService;
import api.contracts.enums.TransactionStatus;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class CottageService implements ICottageService {
    @PersistenceContext
    private EntityManager em;
    @Inject
    private ISimpleEntityManager sem;
    @Inject
    private IPaymentsService paymentsService;
    @Inject
    private IUserService userService;

    @Override
    public List<Cottage> getByFilters(String title, int beds, String dateFrom, String dateTo, int priceFrom, int priceTo) {
        String titleFilter = title != null ? '%' + title + '%' : "";
        dateFrom = dateFrom == null ? DateTime.now().toString("YYYY-MM-dd") : dateFrom;
        dateTo = dateTo == null ? DateTime.now().plusYears(1).toString("YYYY-MM-dd") : dateTo;

        if (priceTo == 0) {
            priceTo = Integer.MAX_VALUE;
        }

        Query query = em.createNativeQuery("\n" +
                "WITH a AS (\n" +
                "\n" +
                "  SELECT c.id FROM main.cottages c\n  " +
                "  WHERE (:title = '' OR lower(C.title) LIKE lower(:title))\n" +
                "        AND (:beds = 0 OR c.bedcount = :beds)\n" +
                "        AND\n" +
                "        (((EXTRACT(MONTH FROM c.availablefrom) BETWEEN EXTRACT(MONTH FROM cast(:dateFrom AS DATE)) AND EXTRACT(MONTH FROM cast(:dateTo AS DATE))\n" +
                "           AND  EXTRACT(MONTH FROM c.availablefrom) BETWEEN EXTRACT(DOY FROM cast(:dateFrom AS DATE)) AND EXTRACT(DOY FROM cast(:dateTo AS DATE)))\n" +
                "          OR\n" +
                "          (EXTRACT(MONTH FROM cast(:dateFrom AS DATE)) BETWEEN EXTRACT(MONTH FROM c.availablefrom) AND EXTRACT(MONTH FROM c.availableto)\n" +
                "           AND EXTRACT(DOY FROM cast(:dateFrom AS DATE)) BETWEEN EXTRACT(DOY FROM c.availablefrom) AND EXTRACT(DOY FROM c.availableto)))\n" +
                "         OR\n" +
                "         ((EXTRACT(MONTH FROM c.availableto) BETWEEN EXTRACT(MONTH FROM cast(:dateFrom AS DATE)) AND EXTRACT(MONTH FROM cast(:dateTo AS DATE))\n" +
                "           AND  EXTRACT(MONTH FROM c.availableto) BETWEEN EXTRACT(DOY FROM cast(:dateFrom AS DATE)) AND EXTRACT(DOY FROM cast(:dateTo AS DATE)))\n" +
                "          OR\n" +
                "          (EXTRACT(MONTH FROM cast(:dateTo AS DATE)) BETWEEN EXTRACT(MONTH FROM c.availablefrom) AND EXTRACT(MONTH FROM c.availableto)\n" +
                "           AND EXTRACT(DOY FROM cast(:dateTo AS DATE)) BETWEEN EXTRACT(DOY FROM c.availablefrom) AND EXTRACT(DOY FROM c.availableto))))\n" +
                "        AND (c.price BETWEEN :priceFrom AND :priceTo) " +
                "  EXCEPT\n" +
                "\n" +
                "  SELECT r.cottageid\n" +
                "  FROM main.reservations r\n" +
                "    INNER JOIN main.cottages c ON r.cottageid = c.id\n" +
                "  WHERE r.cancelled = FALSE AND :dateTo NOT LIKE '' AND :dateFrom NOT LIKE '' AND r.datefrom BETWEEN cast(:dateFrom AS DATE) AND cast(:dateTo AS DATE)\n" +
                "  GROUP BY r.cottageid, c.availableto, c.availablefrom\n" +
                "  HAVING SUM(LEAST(r.dateTo , cast(:dateTo AS DATE)) -  r.datefrom) >= (cast(LEAST((c.availableto + cast(((EXTRACT(YEAR FROM cast(:dateTo AS DATE)) - EXTRACT(YEAR FROM c.availableto))|| ' year') AS INTERVAL)), cast(:dateTo AS DATE)) AS DATE) -  cast(GREATEST((c.availableFrom + cast(((EXTRACT(YEAR FROM cast(:dateFrom AS DATE)) - EXTRACT(YEAR FROM c.availablefrom))|| ' year') AS INTERVAL)), cast(:dateFrom AS DATE)) AS DATE))\n" +
                ")\n" +
                "\n" +
                "SELECT C.* FROM main.cottages C\n" +
                "  INNER JOIN a aa ON C.id = aa.id\n" +
                "ORDER BY C.id;", Cottage.class)
                .setParameter("title", titleFilter)
                .setParameter("beds", beds)
                .setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo)
                .setParameter("priceFrom", priceFrom)
                .setParameter("priceTo", priceTo);

        return query.getResultList();
    }

    public List<Cottage> getAvailableCottagesForFullPeriod(LocalDate from, LocalDate to) {
        List<Cottage> cottages = new ArrayList<>();
        while (from.isBefore(to)) {
            LocalDate newTo = from.plusWeeks(1);
            cottages.addAll(getByFilters("", 0, from.toString("YYYY-MM-dd"), newTo.toString("YYYY-MM-dd"), 0, 0));
            from = newTo;
        }

        return cottages;
    }

    @Override
    public void save(Cottage cottage) {
        try {
            em.persist(cottage);
            em.flush();
        } catch (Exception e) {
            em.clear();
            throw e;
        }
    }

    @Override
    public Cottage get(int id) {
        return em.find(Cottage.class, id);
    }

    @Override
    public void delete(int id) {
        Cottage cottage = get(id);
        em.remove(cottage);
    }

    @Override
    public List<Service> getCottageServices(int id) {
        return em.createQuery("SELECT S FROM Service S WHERE S.cottage.id = :cottageId", Service.class).setParameter("cottageId", id).getResultList();
    }

    public boolean isNowReservationPeriod() {
        return (Boolean) em.createNativeQuery("SELECT " +
                "EXISTS(" +
                "SELECT * FROM main.reservationsperiods " +
                "WHERE :dateToCheck BETWEEN fromdate AND todate)")
                .setParameter("dateToCheck", new Date())
                .getSingleResult();
    }

    @Override
    public List<Reservation> getUpcomingReservations() {
        Query q = em.createQuery("SELECT R FROM Reservation R WHERE R.dateFrom > :date AND R.cancelled = FALSE", Reservation.class).setParameter("date", DateTime.now().toDate());

        return q.getResultList();
    }

    @Override
    public List<Reservation> getPassedReservations() {
        Query q = em.createQuery("SELECT R FROM Reservation R WHERE R.dateTo < :date AND R.cancelled = FALSE", Reservation.class).setParameter("date", DateTime.now().toDate());

        return q.getResultList();
    }

    @Override
    public List<Reservation> getReservations() {
        Query q = em.createQuery("SELECT R FROM Reservation R WHERE R.cancelled = FALSE", Reservation.class);

        return q.getResultList();
    }

    public void saveReservationPeriod(DateTime from, DateTime to) {
        ReservationsPeriod rp = new ReservationsPeriod();
        rp.setFromdate(new java.sql.Date(from.getMillis()));
        rp.setTodate(new java.sql.Date(to.getMillis()));

        sem.insert(rp);
    }

    public List<ReservationsPeriod> getReservationPeriods(String fromDate, String toDate) {
        toDate = toDate == null ? DateTime.now().plusYears(10).toString("YYYY-MM-dd") : toDate;
        fromDate = fromDate == null ? DateTime.now().minusYears(10).toString("YYYY-MM-dd") : toDate;

        return em.createNativeQuery("SELECT rp.* FROM main.reservationsperiods rp " +
                "WHERE :fromDate IS NULL OR :toDate IS NULL OR rp.fromDate BETWEEN CAST(:fromDate AS DATE) AND CAST(:toDate AS DATE) " +
                "OR CAST(:fromDate AS DATE) BETWEEN rp.fromDate AND rp.toDate " +
                "OR rp.toDate BETWEEN CAST(:fromDate AS DATE) AND CAST(:toDate AS DATE) " +
                "OR CAST(:toDate AS DATE) BETWEEN rp.fromDate AND rp.toDate", ReservationsPeriod.class)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .getResultList();
    }

    @Override
    public boolean cancelReservation(int id) {
        Reservation reservation = sem.getById(Reservation.class, id);
        int status = reservation.getStatus();

        if (status == TransactionStatus.pending.getValue()) {
            reservation.setCancelled(true);
            reservation.getPayment().setActive(false);
            return true;
        }

        if (status == TransactionStatus.approved.getValue()) {
            reservation.getPayment().getTransactions().stream().forEach(t -> t.setStatus(TransactionStatus.cancelled.getValue()));
            reservation.setCancelled(true);
            reservation.getPayment().setActive(false);

            String currency = reservation.getPayment().getCurrency();
            if (currency.equals("EUR")) {
                paymentsService.createGift(reservation.getUser().getId(),
                        String.format("Refund for reservation of cottage \"%s\".", reservation.getCottage().getTitle()),
                        reservation.getPayment().calculatePrice());
            }

            return true;
        }

        if (status == TransactionStatus.cancelled.getValue()) {
            return false;
        }

        if (status == TransactionStatus.failed.getValue()) {
            reservation.setCancelled(true);
            return false;
        }

        return false;
    }

    public boolean isGroupAvailable(){
        int rg = userService.get().activeGroup();
        if(rg == 0) return false;

        DateTime periodStart = getCurrentPeriodStartDate();
        boolean isGroupTime = DateTime.now().isAfter(periodStart.plusWeeks(rg -1));

        return isGroupTime;
    }

    public DateTime getCurrentPeriodStartDate(){
        Date date = (Date)em.createNativeQuery("SELECT fromdate FROM main.reservationsperiods " +
                "WHERE :dateToCheck BETWEEN fromdate AND todate")
                .setParameter("dateToCheck", new Date())
                .getSingleResult();

        return new DateTime(date.getTime());
    }
}
