document.addEventListener('DOMContentLoaded', function () {
    const bookingForm = document.getElementById('tourBookingForm');
    if (bookingForm) {
        bookingForm.addEventListener('submit', function (e) {
            // Trigger HTML5 validation UI if the form is invalid
            if (!bookingForm.checkValidity()) {
                e.preventDefault();
                bookingForm.reportValidity();
                return;
            }

            // Check for duplicate CCCD/Passport
            const identityInputs = document.querySelectorAll('input[name$=".identityNumber"]');
            const identityValues = [];
            const duplicateValues = new Set();
            const duplicateErrorAlert = document.getElementById('duplicateErrorAlert');

            // Reset previous highlights
            document.querySelectorAll('.customer-card').forEach(card => {
                card.classList.remove('border-danger', 'border-2', 'shadow-sm');
            });
            if (duplicateErrorAlert) {
                duplicateErrorAlert.classList.add('d-none');
            }

            identityInputs.forEach(input => {
                const val = input.value.trim();
                if (val !== "") {
                    if (identityValues.includes(val)) {
                        duplicateValues.add(val);
                    } else {
                        identityValues.push(val);
                    }
                }
            });

            if (duplicateValues.size > 0) {
                e.preventDefault();
                
                // Highlight the cards with duplicate identities
                identityInputs.forEach(input => {
                    if (duplicateValues.has(input.value.trim())) {
                        const card = input.closest('.customer-card');
                        if (card) {
                            card.classList.add('border-danger', 'border-2', 'shadow-sm');
                            card.style.transition = 'all 0.3s ease';
                        }
                    }
                });

                if (duplicateErrorAlert) {
                    duplicateErrorAlert.classList.remove('d-none');
                }
                return;
            }

            // Add loading state to button to prevent double submission
            const btn = bookingForm.querySelector('button[type="submit"]');
            if (btn) {
                btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Đang xử lý...';
                btn.classList.add('disabled');
            }
        });
    }
});
