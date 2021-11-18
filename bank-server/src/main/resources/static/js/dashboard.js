var BankDashboard = function (settings) {
    this.settings = settings;
    this.init();
};

BankDashboard.prototype = {
    init: function () {
        this.container = this.getElement(this.settings.elements.container);
        this.reportContainer = this.getElement(this.settings.elements.reportContainer);
        this.accountSpinner = this.getElement(this.settings.elements.accountSpinner);
        this.refreshButton = this.getElement(this.settings.elements.refreshButton);
        this.loadInitialState();
        this.addWebsocketListener();
    },

    loadInitialState: function () {
        var _this = this;

        $.get(this.settings.endpoints.currencies, function (data) {
            _this.createReportElements(data);
        });

        $.get(this.settings.endpoints.topAccounts, function (data) {
            _this.createAccountElements(data['_embedded']['roachbank:account-list']);
        });
    },

    getElement: function (id) {
        return $('#' + id);
    },

    createAccountElement: function (account) {
        var _this = this, view;

        var countryCode = ((_this.settings.regionCountry[account.region]) ? _this.settings.regionCountry[account.region] : account.region);
        view = $('<div>')
                    .attr('id', account.id)
                    .attr('data-toggle', 'tooltip')
                    .addClass('box')
                    .css(_this.boxSize(account.region, account.balance.currency, account.balance.amount))
                    .css({
                        background: _this.boxColor(account.region, account.currency, account.balance)
                    })
                    .append(
                            $('<span>')
                                    .addClass('amount')
                                    .text(_this.formatMoney(account.balance, account.currency))
                    )
                    .append(
                            $('<span>')
                                    .addClass('name')
                                    .text(account.name)
                    )
                    .append(
                            $('<span>')
                                    .addClass('region')
                                    .text(account.region)
                    )
                    .append(
                            $('<span>')
                                    .addClass('flag')
                                    .append($('<a>')
                                            .append(
                                                    $('<img>')
                                                            .addClass('img-fluid')
                                                            .attr("src", "/images/flags/" + countryCode + ".png")
                                            )
                                             .attr('href', account.href))
                    );
        this.container.append(view);
    },

    createAccountElements: function (data) {
        var _this = this, accounts;

        _this.accountSpinner.remove();

        accounts = data.map(function (account) {
            var countryCode = ((_this.settings.regionCountry[account.region]) ? _this.settings.regionCountry[account.region] : account.region);

            return $('<div>')
                    .attr('id', account.id)
                    .attr('data-toggle', 'tooltip')
                    .attr('title', account._links.self.href)
                    .addClass('box')
                    .css(_this.boxSize(account.region, account.balance.currency, account.balance.amount))
                    .css({
                        background: _this.boxColor(account.region, account.balance.currency, account.balance.amount)
                    })
                    .append(
                            $('<span>')
                                    .addClass('amount')
                                    .text(_this.formatMoney(account.balance.amount, account.balance.currency))
                    )
                    .append(
                            $('<span>')
                                    .addClass('name')
                                    .text(account.name)
                    )
                    .append(
                            $('<span>')
                                    .addClass('region')
                                    .text(account.region)
                    )
                    .append(
                            $('<span>')
                                    .addClass('flag')
                                    .append($('<a>')
                                            .append(
                                                    $('<img>')
                                                            .addClass('img-fluid')
                                                            .attr("src", "/images/flags/" + countryCode + ".png")
                                            )
                                            .attr('href', account._links.self.href))
                    )
        });

        this.container.append(accounts);
    },

    createReportElements: function (data) {
        var _this = this, report;

        report = data.map(function (currency) {
            return $('<tr>')
                    .append(
                            $('<th>')
                                    .attr('scope', 'row')
                                    .text(currency)
                    )
                    .append(
                            $('<td>')
                                    .attr('id', currency + "-total-regions")
                                    .text("0")
                    )
                    .append(
                            $('<td>')
                                    .attr('id', currency + "-total-accounts")
                                    .text("0")
                    )
                    .append(
                            $('<td>')
                                    .attr('id', currency + "-total-transactions")
                                    .text("0")
                    )
                    .append(
                            $('<td>')
                                    .attr('id', currency + "-total-transaction-legs")
                                    .text("0")
                    )
                    .append(
                            $('<td>')
                                    .attr('id', currency + "-total-balance")
                                    .text(_this.formatMoney("0.00", "USD"))
                    )
                    .append(
                            $('<td>')
                                    .attr('id', currency + "-total-turnover")
                                    .text(_this.formatMoney("0.00", "USD"))
                    )
                    .append(
                            $('<td>')
                                    .attr('id', currency + "-total-checksum")
                                    .text(_this.formatMoney("0.00", "USD"))
                    )
        });

        this.reportContainer.append(report);
    },

    addWebsocketListener: function () {
        var socket = new SockJS(this.settings.endpoints.socket),
                stompClient = Stomp.over(socket),
                _this = this;

        stompClient.connect({}, function (frame) {
            stompClient.subscribe(_this.settings.topics.reportAccountSummary, function (report) {
                var event = JSON.parse(report.body);

                sessionStorage.setItem("account-summary-" + event.currency, event.maxBalance);

                _this.handleAccountSummaryUpdate(event);
            });

            stompClient.subscribe(_this.settings.topics.reportTransactionSummary, function (report) {
                if (report.body == "") {
                    _this.refreshButton.prop("disabled", false);
                    _this.refreshButton.text("Refresh Now");
                } else {
                    var event = JSON.parse(report.body);
                    _this.handleTransactionSummaryUpdate(event);
                }
            });

            stompClient.subscribe(_this.settings.topics.accounts, function (account) {
                var event = JSON.parse(account.body); // batch

                event.map(function (item) {
                    var accountElt = _this.getElement(item.id);
                    if (accountElt) {
                        _this.handleAccountBalanceUpdate(accountElt, item.region, item.currency, item.balance);
                    }
                });
            });
        });
    },

    handleAccountBalanceUpdate: function (account, region, currency, balance) {
        var _this = this;

        var original_color = _this.boxColor(region, currency, balance);
        account.css("background-color", "white");

        var m = _this.formatMoney(balance, currency);
        account.find('.amount').text(m);

        setTimeout( function(){
            account.css("background-color", original_color);
        }, 1500);
    },

    handleAccountSummaryUpdate: function (accountSummary) {
        var _this = this;

        var totalRegionsSuffix = _this.getElement(accountSummary.currency + _this.settings.elements.totalRegionsSuffix);
        var totalAccountSuffix = _this.getElement(accountSummary.currency + _this.settings.elements.totalAccountSuffix);
        var totalBalanceSuffix = _this.getElement(accountSummary.currency + _this.settings.elements.totalBalanceSuffix);

        totalRegionsSuffix.text(_this.formatNumber(accountSummary.numberOfRegions));
        totalAccountSuffix.text(_this.formatNumber(accountSummary.numberOfAccounts));
        totalBalanceSuffix.text(_this.formatMoney(accountSummary.totalBalance, accountSummary.currency));
    },

    handleTransactionSummaryUpdate: function (transactionSummary) {
        var _this = this;

        var totalTransactionsSuffix = _this.getElement(
                transactionSummary.currency + _this.settings.elements.totalTransactionsSuffix);
        var totalTransactionLegsSuffix = _this.getElement(
                transactionSummary.currency + _this.settings.elements.totalTransactionLegsSuffix);
        var totalTurnoverSuffix = _this.getElement(
                transactionSummary.currency + _this.settings.elements.totalTurnoverSuffix);
        var totalChecksumSuffix = _this.getElement(
                transactionSummary.currency + _this.settings.elements.totalChecksumSuffix);

        totalTransactionsSuffix.text(_this.formatNumber(transactionSummary.numberOfTransactions));
        totalTransactionLegsSuffix.text(_this.formatNumber(transactionSummary.numberOfLegs));
        totalTurnoverSuffix.text(_this.formatMoney(transactionSummary.totalTurnover, transactionSummary.currency));
        totalChecksumSuffix.text(_this.formatMoney(transactionSummary.totalCheckSum, transactionSummary.currency));
    },

    boxSize: function (region, currency, amount) {
        var size = 45;
        return {
            width: size + 'px',
            height: size + 'px',
            lineHeight: size + 'px',
        }
    },

    boxColor: function (region, currency, amount) {
        var random = this.settings.regionColors[Math.floor(Math.random() * this.settings.regionColors.length)];
        var hex = ((this.settings.regionColors[region]) ? this.settings.regionColors[region] : random);

        var maxBalance = sessionStorage.getItem("account-summary-" + currency);
        if (!maxBalance) {
            maxBalance = 0;
        }

        return this.hexToRgbA(hex, amount, maxBalance);
    },

    hexToRgbA: function (hex, amount, maxAmount) {
        if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
            var c = hex.substring(1).split('');
            if (c.length == 3) {
                c = [c[0], c[0], c[1], c[1], c[2], c[2]];
            }
            c = '0x' + c.join('');
            var a;
            if (maxAmount > 0) {
                a = Math.max(0.5, amount / maxAmount);
            } else {
                a = 1;
            }
            return 'rgba(' + [(c >> 16) & 255, (c >> 8) & 255, c & 255, a].join(',') + ')';
        }
        console.log("WARN: bad color hex " + hex);
        return "rgba(1,0.5,1,1)";
    },

    formatMoney: function (number, currency) {
        var formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency,
        });
        return formatter.format(number);
    },

    formatNumber: function (number) {
        var formatter = new Intl.NumberFormat('en-US', {
            maximumSignificantDigits: 3
        });
        return formatter.format(number);
    }
};

document.addEventListener('DOMContentLoaded', function () {
    new BankDashboard({
        endpoints: {
            topAccounts: '/api/account/top',
            currencies: '/api/metadata/currencies',
            socket: '/roach-bank'
        },

        topics: {
            reportAccountSummary: '/topic/account-summary',
            reportTransactionSummary: '/topic/transaction-summary',
            accounts: '/topic/accounts'
        },

        elements: {
            container: 'dashboard-container',
            accountSpinner: 'account-spinner',
            refreshButton: 'refresh-button',
            reportContainer: 'report-container',
            totalRegionsSuffix: '-total-regions',
            totalAccountSuffix: '-total-accounts',
            totalTransactionsSuffix: '-total-transactions',
            totalTransactionLegsSuffix: '-total-transaction-legs',
            totalBalanceSuffix: '-total-balance',
            totalTurnoverSuffix: '-total-turnover',
            totalChecksumSuffix: '-total-checksum'
        },

        regionColors: {
            'seattle': '#c15000',
            'san francisco': '#187e98',
            'los angeles': '#89218e',
            'phoenix': '#844099',
            'minneapolis': '#0f625c',
            'chicago': '#5f7c4b',
            'detroit': '#443287',
            'atlanta': '#ad1baf',
            'new york': '#274c60',
            'boston': '#8a5827',
            'washington dc': '#c15050',
            'miami': '#c15055',
            'stockholm': '#afaf17',
            'helsinki': '#8295ea',
            'oslo': '#f04f4f',
            'london': '#0bdb1f',
            'frankfurt': '#22c8b0',
            'amsterdam': '#5f7b51',
            'paris': '#8dc079',
            'milano': '#b736f0',
            'madrid': '#f79e8b',
            'athens': '#28b9ed',
            'singapore': '#9036df',
            'hong kong': '#67b5db',
            'sydney': '#ba8412',
            'tokyo': '#2f8d65',
            'barcelona': '#854231',
            'manchester': '#8a0615',
            'sao paulo': '#5f7b51',
            'rio de janeiro': '#443287',
            'salvador': '#89218e'
        },

        regionCountry: {
            'generic': 'GEN',
            'tokyo': 'JPN',
            'barcelona': 'ESP',
            'seattle': 'USA',
            'san francisco': 'USA',
            'los angeles': 'USA',
            'phoenix': 'USA',
            'minneapolis': 'USA',
            'chicago': 'USA',
            'detroit': 'USA',
            'atlanta': 'USA',
            'new york': 'USA',
            'boston': 'USA',
            'washington dc': 'USA',
            'miami': 'USA',
            'stockholm': 'SWE',
            'helsinki': 'FIN',
            'oslo': 'NOR',
            'london': 'GBR',
            'manchester': 'GBR',
            'frankfurt': 'DEU',
            'amsterdam': 'NLD',
            'paris': 'FRA',
            'milano': 'ITA',
            'madrid': 'ESP',
            'athens': 'GRC',
            'singapore': 'SGP',
            'hong kong': 'HKG',
            'sydney': 'AUS',
            'sao paulo': 'BRL',
            'rio de janeiro': 'BRL',
            'salvador': 'BRL'
        }
    });
});

