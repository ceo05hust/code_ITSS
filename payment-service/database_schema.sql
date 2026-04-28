-- Script tạo bảng Database theo đúng thiết kế chung của nhóm ITSS
-- Lưu ý: Đã fix lỗi logic nghiệp vụ ở bảng Invoice (cột transactionId cho phép NULL)

CREATE TABLE TransactionInfo (
    transactionId SERIAL PRIMARY KEY,
    paymentMethod VARCHAR(100),
    transactionContent TEXT,
    transactionDatetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(50)
);

CREATE TABLE Invoice (
    invoiceId SERIAL PRIMARY KEY,
    -- Bỏ NOT NULL ở đây vì khi mới khởi tạo Invoice thì chưa có Transaction
    transactionId INTEGER UNIQUE,
    totalProductPriceExclVAT NUMERIC(12,2) NOT NULL,
    totalProductPriceInclVAT NUMERIC(12,2) NOT NULL,
    shippingFee NUMERIC(12,2) NOT NULL,
    totalAmount NUMERIC(12,2) NOT NULL,
    CONSTRAINT fkInvoiceTransaction
        FOREIGN KEY (transactionId)
        REFERENCES TransactionInfo(transactionId)
);

CREATE TABLE Orders (
    orderId SERIAL PRIMARY KEY,
    invoiceId INTEGER UNIQUE NOT NULL,
    deliveryInfoId INTEGER UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fkOrderInvoice
        FOREIGN KEY (invoiceId)
        REFERENCES Invoice(invoiceId)
    -- Giả sử bảng DeliveryInfo đã được tạo trước đó
    -- CONSTRAINT fkOrderDeliveryInfo
    --    FOREIGN KEY (deliveryInfoId)
    --    REFERENCES DeliveryInfo(deliveryInfoId)
);
