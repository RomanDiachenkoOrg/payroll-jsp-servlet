/*
 * Acme Payroll - page behaviour (jQuery 1.12 / jQuery UI 1.12)
 */
(function ($) {
    'use strict';

    function initDatepickers() {
        $('input.date').each(function () {
            var $input = $(this);
            var options = {dateFormat: 'yy-mm-dd', changeMonth: true, changeYear: true, yearRange: '-40:+2'};
            if ($input.hasClass('monday')) {
                options.firstDay = 1;
                options.beforeShowDay = function (d) {
                    return [d.getDay() === 1, ''];
                };
            }
            $input.datepicker(options);
        });
    }

    /* Forms with class "confirm" ask before submitting, using a jQuery UI dialog. */
    function initConfirmForms() {
        $(document).on('submit', 'form.confirm', function (e) {
            var form = this;
            if ($(form).data('confirmed')) {
                return true;
            }
            e.preventDefault();
            $('#confirm-text').text($(form).data('confirm') || 'Are you sure?');
            $('#confirm-dialog').dialog({
                modal: true,
                resizable: false,
                width: 400,
                buttons: {
                    'OK': function () {
                        $(this).dialog('close');
                        $(form).data('confirmed', true);
                        form.submit();
                    },
                    'Cancel': function () {
                        $(this).dialog('close');
                    }
                }
            });
            return false;
        });
    }

    function initRejectDialog() {
        var $dialog = $('#reject-dialog');
        if (!$dialog.length) {
            return;
        }
        $('#reject-timesheet').click(function () {
            $dialog.dialog({
                modal: true,
                width: 420,
                buttons: {
                    'Reject': function () {
                        var reason = $.trim($('#reject-reason-input').val());
                        if (!reason) {
                            $('#reject-reason-error').show();
                            return;
                        }
                        $('#reject-form').submit();
                    },
                    'Cancel': function () {
                        $(this).dialog('close');
                    }
                }
            });
        });
    }

    function initPayTypeToggle() {
        var $radios = $('input[name=payType]');
        if (!$radios.length) {
            return;
        }
        function update() {
            var type = $radios.filter(':checked').val();
            $('.pay-salaried').toggle(type === 'SALARIED');
            $('.pay-hourly').toggle(type === 'HOURLY');
        }
        $radios.change(update);
        update();
    }

    function initHoursTotal() {
        var $inputs = $('.hours-input');
        if (!$inputs.length) {
            return;
        }
        function sum() {
            var total = 0;
            $inputs.each(function () {
                var v = parseFloat($(this).val());
                if (!isNaN(v)) {
                    total += v;
                }
            });
            $('#total-hours').text(Math.round(total * 100) / 100);
        }
        $inputs.on('keyup change', sum);
    }

    $(function () {
        initDatepickers();
        initConfirmForms();
        initRejectDialog();
        initPayTypeToggle();
        initHoursTotal();
        if ($('#tabs').length) {
            $('#tabs').tabs();
        }
        $('#flash-message').delay(6000).fadeOut('slow');
    });
}(jQuery));
