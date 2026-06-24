// Simple Cart Logic for UI demonstration
let cart = [];
let preserveCartOnUnload = false;

window.addEventListener('beforeunload', function() {
    if (!preserveCartOnUnload) {
        sessionStorage.removeItem('food_order_cart');
    }
});

function saveCart() {
    sessionStorage.setItem('food_order_cart', JSON.stringify(cart));
}

function addToCart(id, name, price) {
    const existingItem = cart.find(item => item.id === id);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({ id, name, price, quantity: 1 });
    }
    saveCart();
    updateCartUI();
}

function removeFromCart(index) {
    cart.splice(index, 1);
    saveCart();
    updateCartUI();
}

function updateCartUI() {
    const cartItemsContainer = document.getElementById('cart-items');
    const cartTotalAmount = document.getElementById('cart-total-amount');
    const cartSubtotal = document.getElementById('cart-subtotal');
    const cartTax = document.getElementById('cart-tax');

    if (!cartItemsContainer || !cartTotalAmount) return;

    if (cart.length === 0) {
        cartItemsContainer.innerHTML = '<p style="color: var(--text-light); font-size: 14px; font-style: italic;">Chưa có món nào được chọn.</p>';
        if (cartSubtotal) cartSubtotal.textContent = '0đ';
        if (cartTax) cartTax.textContent = '0đ';
        cartTotalAmount.textContent = '0đ';
        return;
    }

    cartItemsContainer.innerHTML = '';
    let subtotal = 0;

    cart.forEach((item, index) => {
        const itemTotal = item.price * item.quantity;
        subtotal += itemTotal;
        const itemEl = document.createElement('div');
        itemEl.className = 'cart-item';
        itemEl.style.display = 'block';
        itemEl.style.marginBottom = '15px';
        itemEl.style.borderBottom = '1px dashed var(--border-color)';
        itemEl.style.paddingBottom = '10px';
        itemEl.innerHTML = `
            <div style="font-weight: 600; color: var(--primary-color); margin-bottom: 8px; overflow-wrap: break-word; line-height: 1.4; font-size: 14px;">${item.name}</div>
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="color: var(--text-light); font-size: 13px;">Số lượng: <strong style="color: var(--secondary); margin-left: 2px;">x${item.quantity}</strong></span>
                <div style="display: flex; align-items: center;">
                    <span style="color: var(--secondary-dark); font-weight: 600; font-size: 15px;">${itemTotal.toLocaleString('vi-VN')}đ</span>
                    <button type="button" onclick="removeFromCart(${index})" title="Xóa món này" style="background: none; border: none; color: #cc0000; cursor: pointer; margin-left: 12px; font-size: 14px; transition: 0.2s;"><i class="fas fa-times"></i></button>
                </div>
            </div>
        `;
        cartItemsContainer.appendChild(itemEl);
    });

    const tax = Math.round(subtotal * 0.08);
    const total = subtotal + tax;

    if (cartSubtotal) cartSubtotal.textContent = subtotal.toLocaleString('vi-VN') + 'đ';
    if (cartTax) cartTax.textContent = tax.toLocaleString('vi-VN') + 'đ';
    cartTotalAmount.textContent = total.toLocaleString('vi-VN') + 'đ';
}

document.addEventListener('DOMContentLoaded', () => {
    const saved = sessionStorage.getItem('food_order_cart');
    if (saved) {
        try {
            cart = JSON.parse(saved);
            updateCartUI();
        } catch (e) {
            cart = [];
        }
    }

    function validateFormInputs(form) {
        let isValid = true;
        const requiredInputs = form.querySelectorAll('input[required], select[required], textarea[required]');
        requiredInputs.forEach(input => {
            const existingError = input.closest('.form-group').querySelector('.error-message');
            if (existingError) {
                existingError.remove();
            }

            input.style.borderColor = ''; // reset border

            if (!input.value.trim()) {
                isValid = false;
                input.style.borderColor = 'red';
                const errorMessage = document.createElement('div');
                errorMessage.className = 'error-message';
                errorMessage.style.color = 'red';
                errorMessage.style.fontSize = '13px';
                errorMessage.style.marginTop = '5px';
                errorMessage.textContent = 'Vui lòng không để trống thông tin này.';

                // Add event listener to hide error when typing
                input.addEventListener('input', function removeError() {
                    const error = input.closest('.form-group').querySelector('.error-message');
                    if (error) error.remove();
                    input.style.borderColor = '';
                    input.removeEventListener('input', removeError);
                });

                input.closest('.form-group').appendChild(errorMessage);
            }
        });
        return isValid;
    }

    function showActionError(referenceElement, message) {
        let errorEl = referenceElement.previousElementSibling;
        if (!errorEl || !errorEl.classList.contains('action-error-message')) {
            errorEl = document.createElement('div');
            errorEl.className = 'action-error-message';
            errorEl.style.color = 'red';
            errorEl.style.fontSize = '13px';
            errorEl.style.marginBottom = '10px';
            errorEl.style.textAlign = 'center';
            referenceElement.parentNode.insertBefore(errorEl, referenceElement);
        }
        errorEl.textContent = message;

        setTimeout(() => {
            if (errorEl.parentNode) {
                errorEl.remove();
            }
        }, 4000);
    }

    // Init order button handling
    const btnInitOrder = document.getElementById('btn-init-order');
    if (btnInitOrder) {
        btnInitOrder.addEventListener('click', () => {
            if (cart.length === 0) {
                showActionError(btnInitOrder, 'Vui lòng chọn ít nhất một món ăn trước khi đặt!');
                return;
            }

            if (typeof isAuthenticated !== 'undefined' && !isAuthenticated) {
                preserveCartOnUnload = true;
                saveCart();
                window.location.href = '/customer/food-order/checkout';
                return;
            }

            if (btnInitOrder.previousElementSibling && btnInitOrder.previousElementSibling.classList.contains('action-error-message')) {
                btnInitOrder.previousElementSibling.remove();
            }
            btnInitOrder.style.display = 'none';
            const roomOrderForm = document.getElementById('room-order-form');
            if (roomOrderForm) {
                roomOrderForm.style.display = 'block';

                // No payment method styling needed anymore since we removed VNPay
            }
        });
    }

    // Room order form submission
    const roomOrderForm = document.getElementById('room-order-form');
    if (roomOrderForm) {
        roomOrderForm.addEventListener('submit', (e) => {
            e.preventDefault();

            if (!validateFormInputs(roomOrderForm)) {
                return;
            }

            if (cart.length === 0) {
                const submitBtn = roomOrderForm.querySelector('button[type="submit"]');
                if (submitBtn) {
                    showActionError(submitBtn, 'Vui lòng chọn ít nhất một món ăn!');
                }
                return;
            }
            // Dynamically create hidden inputs for itemIds and quantities
            // First remove any old ones if form was submitted before but failed validation
            roomOrderForm.querySelectorAll('input[name="itemIds"]').forEach(el => el.remove());
            roomOrderForm.querySelectorAll('input[name="quantities"]').forEach(el => el.remove());

            cart.forEach(item => {
                const idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'itemIds';
                idInput.value = item.id;
                roomOrderForm.appendChild(idInput);

                const qtyInput = document.createElement('input');
                qtyInput.type = 'hidden';
                qtyInput.name = 'quantities';
                qtyInput.value = item.quantity;
                roomOrderForm.appendChild(qtyInput);
            });

            preserveCartOnUnload = true;
            // Allow form to submit naturally to backend
            roomOrderForm.submit();
        });
    }

    // Table booking form validation
    const tableBookingForm = document.getElementById('table-booking-form');
    if (tableBookingForm) {
        tableBookingForm.addEventListener('submit', (e) => {
            if (!validateFormInputs(tableBookingForm)) {
                e.preventDefault();
                return;
            }
            // Allow natural POST submission
        });
    }

    // Set minimum date for booking to today
    const dateInput = document.getElementById('booking-date');
    if (dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.min = today;
    }

    // Auto-trigger checkout if returning from login
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('checkout') === 'true' && cart.length > 0) {
        if (btnInitOrder) {
            setTimeout(() => {
                btnInitOrder.click();
            }, 300);
        }
    }

});

function filterMenu(categoryId) {
    // Show/Hide categories
    const categories = document.querySelectorAll('.category-section');
    categories.forEach(cat => {
        if (categoryId === 'all') {
            cat.style.display = 'block';
        } else {
            if (cat.id === 'category-section-' + categoryId) {
                cat.style.display = 'block';
            } else {
                cat.style.display = 'none';
            }
        }
    });
}


function toggleCategoryDropdown() {
    document.getElementById('customCategorySelect').classList.toggle('open');
}

function selectCategory(categoryId, categoryName) {
    // Update UI
    document.getElementById('customSelectText').innerText = categoryName;
    document.getElementById('customCategorySelect').classList.remove('open');

    // Update selected class on options
    const options = document.querySelectorAll('.custom-option');
    options.forEach(opt => opt.classList.remove('selected'));
    event.currentTarget.classList.add('selected');

    // Call existing filterMenu logic
    filterMenu(categoryId);
}

// Close dropdown when clicking outside
document.addEventListener('click', function (e) {
    const selectWrapper = document.getElementById('customCategorySelect');
    if (selectWrapper && !selectWrapper.contains(e.target)) {
        selectWrapper.classList.remove('open');
    }
});

// Auto-convert standard selects with class "beautiful-select" into a custom UI
function initTableReservationSelect() {
    const selects = document.querySelectorAll('select.beautiful-select');
    selects.forEach(select => {
        if (select.nextElementSibling && select.nextElementSibling.classList.contains('custom-select-wrapper')) {
            return;
        }

        const wrapper = document.createElement('div');
        wrapper.className = 'custom-select-wrapper beautiful-wrapper';
        wrapper.style.width = '100%';
        
        select.parentNode.insertBefore(wrapper, select.nextSibling);
        wrapper.appendChild(select);
        select.style.display = 'none';

        const trigger = document.createElement('div');
        trigger.className = 'custom-select-trigger';
        
        const span = document.createElement('span');
        span.textContent = select.options[select.selectedIndex]?.text || 'Chọn...';
        trigger.appendChild(span);

        const icon = document.createElement('i');
        icon.className = 'fa-solid fa-chevron-down';
        trigger.appendChild(icon);

        const optionsDiv = document.createElement('div');
        optionsDiv.className = 'custom-select-options';

        const buildOptions = () => {
            optionsDiv.innerHTML = '';
            Array.from(select.options).forEach((option, index) => {
                if (option.style.display === 'none') return;
                
                const optDiv = document.createElement('div');
                optDiv.className = 'custom-option';
                if (option.selected) optDiv.classList.add('selected');
                optDiv.textContent = option.text;

                optDiv.addEventListener('click', (e) => {
                    e.stopPropagation();
                    select.selectedIndex = index;
                    span.textContent = option.text;
                    wrapper.classList.remove('open');
                    
                    select.dispatchEvent(new Event('change'));
                    
                    Array.from(optionsDiv.children).forEach(c => c.classList.remove('selected'));
                    optDiv.classList.add('selected');
                });
                optionsDiv.appendChild(optDiv);
            });
        };

        buildOptions();

        wrapper.appendChild(trigger);
        wrapper.appendChild(optionsDiv);

        trigger.addEventListener('click', (e) => {
            e.stopPropagation();
            document.querySelectorAll('.custom-select-wrapper').forEach(c => {
                if (c !== wrapper && c.id !== 'customCategorySelect') {
                    c.classList.remove('open');
                }
            });
            buildOptions();
            wrapper.classList.toggle('open');
        });

        select.addEventListener('change', () => {
            span.textContent = select.options[select.selectedIndex]?.text || '';
            buildOptions();
        });
    });

    document.addEventListener('click', () => {
        document.querySelectorAll('.custom-select-wrapper.beautiful-wrapper').forEach(c => {
            c.classList.remove('open');
        });
    });
}
document.addEventListener('DOMContentLoaded', initTableReservationSelect);


