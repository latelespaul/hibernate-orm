package org.hibernate.orm.test.jpa.compliance;

import java.util.List;

import org.hibernate.testing.orm.junit.EntityManagerFactoryScope;
import org.hibernate.testing.orm.junit.Jpa;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Jpa(
		annotatedClasses = {
				CriteriaQueryTest.Payment.class,
				CriteriaQueryTest.LineItem.class,
				CriteriaQueryTest.Order.class,
		}
)
public class CriteriaQueryTest {

	@Test
	public void testCriteriaQuery(EntityManagerFactoryScope scope) {
		scope.inTransaction(
				entityManager -> {
					final Payment payment = new Payment( 1, "1111-2222-3333-4444" );

					final Order order = new Order( 2 );
					order.addPayment( payment );

					final LineItem lineItem = new LineItem( 1, "Line item # 1" );
					lineItem.setOrder( order );

					order.setSampleLineItem( lineItem );

					entityManager.persist( payment );
					entityManager.persist( lineItem );
					entityManager.persist( order );
				}
		);

		scope.inTransaction(
				entityManager -> {
					final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

					final CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery( Order.class );
					final Root<Order> order = criteriaQuery.from( Order.class );

					criteriaQuery.select( order );

					List<Order> result = entityManager.createQuery( criteriaQuery ).getResultList();
					assertThat( result.size(), is( 1 ) );
				}
		);
	}

	@Entity(name = "LineItem")
	@Table(name = "LINEITEM_TABLE")
	public static class LineItem {

		private Integer id;

		private String name;

		private Order order;

		public LineItem() {
		}

		public LineItem(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		@Id
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@ManyToOne
		@JoinColumn(name = "LINE_ITEM_ORDER")
		public Order getOrder() {
			return order;
		}

		protected void setOrder(Order order) {
			this.order = order;
		}
	}

	@Entity(name = "Order")
	@Table(name = "ORDER_TABLE")
	public static class Order {

		private Integer id;

		private String orderCode;

		private Payment payment;

		private LineItem sampleLineItem;

		public Order() {
		}

		public Order(Integer id) {
			this.id = id;
		}

		@Id
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getOrderCode() {
			return orderCode;
		}

		public void setOrderCode(String orderCode) {
			this.orderCode = orderCode;
		}

		@OneToOne(mappedBy = "order")
		public Payment getPayment() {
			return payment;
		}

		public void setPayment(Payment payment) {
			this.payment = payment;
		}

		@OneToOne
		@JoinColumn(name = "ORDER_LINEITEM")
		public LineItem getSampleLineItem() {
			return sampleLineItem;
		}

		public void setSampleLineItem(LineItem lineItem) {
			this.sampleLineItem = lineItem;
		}

		public void addPayment(Payment payment) {
			this.payment = payment;
			payment.setOrder( this );
		}
	}

	@Entity(name = "Payment")
	@Table(name = "PAYMENT_TABLE")
	public static class Payment {

		private Integer id;

		private String number;

		private Order order;

		public Payment() {
		}

		public Payment(Integer id, String number) {
			this.id = id;
			this.number = number;
		}

		@Id
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@Column(name = "PAYMENT_NUMBER")
		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}

		@OneToOne
		@JoinColumn(name = "PAYMENT_ORDER")
		public Order getOrder() {
			return order;
		}

		public void setOrder(Order order) {
			this.order = order;
		}
	}
}
