var BankDashboard = function (settings) {
    this.settings = settings;
    this.init();
};

BankDashboard.prototype = {
    init: function () {
        this.container = this.getElement(this.settings.elements.container);
        this.accountSpinner = this.getElement(this.settings.elements.accountSpinner);
        this.loadInitialState();
        this.addWebsocketListener();
    },

    loadInitialState: function () {
        var _this = this;

        const queryString = window.location.search;
        const urlParams = new URLSearchParams(queryString);

        var limit=10;

        if (urlParams.has('limit')) {
            limit = urlParams.get('limit');
        }

        if (urlParams.has('region')) {
            const region = urlParams.get('region');

            $.get(this.settings.endpoints.topAccounts+"?limit="+limit+"&regions="+region, function (data) {
                _this.createAccountElements(data['_embedded']['roachbank:account-list']);
            });
        } else {
            $.get(this.settings.endpoints.topAccounts+"?limit="+limit, function (data) {
                _this.createAccountElements(data['_embedded']['roachbank:account-list']);
            });
        }
    },

    getElement: function (id) {
        return $('#' + id);
    },

    createAccountElements: function (data) {
        var _this = this, accounts;

        _this.accountSpinner.remove();

        accounts = data.map(function (account) {
            var countryCode = ((_this.settings.regionCountry[account.city]) ? _this.settings.regionCountry[account.city] : account.city);

            return $('<div>')
                    .attr('id', account.id)
                    .attr('data-toggle', 'tooltip')
                    .attr('title', account._links.self.href)
                    .addClass('box')
                    .css(_this.boxSize(account.balance.amount, account.balance.currency))
                    .css({
                        background: _this.boxColor(account.city)
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
                                    .addClass('city')
                                    .text(account.city)
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

    addWebsocketListener: function () {
        var socket = new SockJS(this.settings.endpoints.socket),
                stompClient = Stomp.over(socket),
                _this = this;

        stompClient.connect({}, function (frame) {
            stompClient.subscribe(_this.settings.topics.accounts, function (account) {
                var event = JSON.parse(account.body); // batch

                event.map(function (item) {
                    var accountElt = _this.getElement(item.id);
                    if (accountElt) {
                        _this.handleAccountBalanceUpdate(accountElt, item.city, item.currency, item.balance);
                    }
                });
            });
        });
    },

    handleAccountBalanceUpdate: function (account, city, currency, balance) {
        var _this = this;

        var original_color = _this.boxColor(city);
        account.css("background-color", "white");

        var m = _this.formatMoney(balance, currency);
        account.find('.amount').text(m);

        setTimeout( function(){
            account.css("background-color", original_color);
        }, 1500);
    },

    boxSize: function (amount,currency) {
        var size=50;
        return {
            width: size + 'px',
            height: size + 'px',
            lineHeight: size + 'px',
        }
    },

    boxColor: function (city) {
        var random = this.settings.regionColors[Math.floor(Math.random() * this.settings.regionColors.length)];
        var hex = ((this.settings.regionColors[city]) ? this.settings.regionColors[city] : random);
        return this.hexToRgbA(hex);
    },

    hexToRgbA: function (hex) {
        if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
            var c = hex.substring(1).split('');
            if (c.length == 3) {
                c = [c[0], c[0], c[1], c[1], c[2], c[2]];
            }
            c = '0x' + c.join('');
            var a=1;
            return 'rgba(' + [(c >> 16) & 255, (c >> 8) & 255, c & 255, a].join(',') + ')';
        }
        // console.log("WARN: bad color hex " + hex);
        return "rgba(1,0.5,1,1)";
    },

    formatMoney: function (number, currency) {
        var formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency,
        });
        return formatter.format(number);
    }
};

document.addEventListener('DOMContentLoaded', function () {
    new BankDashboard({
        endpoints: {
            topAccounts: '/api/account/top',
            socket: '/roach-bank'
        },

        topics: {
            reportAccountSummary: '/topic/account-summary',
            reportTransactionSummary: '/topic/transaction-summary',
            accounts: '/topic/accounts'
        },

        elements: {
            container: 'dashboard-container',
            accountSpinner: 'account-spinner'
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
            'portland': 'USA',
            'las vegas': 'USA',
            'charlotte': 'USA',
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
            'salvador': 'BRL',
            'buenos aires': 'ARG',

            'copenhagen': 'DNK',
            'riga': 'LVA',
            'tallinn': 'EST',
            'dublin': 'IRL',
            'belfast': 'IRL',
            'liverpool': 'GBR',
            'glasgow': 'GBR',
            'birmingham': 'GBR',
            'leeds': 'GBR',

            'rotterdam': 'NLD',
            'antwerp': 'BEL',
            'hague': 'NLD',
            'ghent': 'BEL',
            'brussels': 'BEL',
            'berlin': 'DEU',
            'hamburg': 'DEU',
            'munich': 'DEU',
            'dusseldorf': 'DEU',
            'leipzig': 'DEU',
            'dortmund': 'DEU',
            'essen': 'DEU',
            'stuttgart': 'DEU',

            'sintra': 'ESP',
            'rome': 'ITA',
            'milan': 'ITA',
            'lyon': 'FRA',
            'lisbon': 'PRT',
            'toulouse': 'FRA',
            'cologne': 'FRA',
            'seville': 'FRA',
            'marseille': 'FRA',
            'naples': 'ITA',
            'turin': 'ITA',
            'valencia': 'ITA',
            'palermo': 'ITA',

            'krakov': 'POL',
            'zagraeb': 'SRB',
            'zaragoza': 'ESP',
            'lodz': 'POL',
            'bratislava': 'SVK',
            'prague': 'ZCE',
            'sofia': 'BGR',
            'bucharest': 'ROU',
            'vienna': 'AUT',
            'warsaw': 'POL',
            'budapest': 'HUN',
            'beijing': 'CHN',
            'shanghai': 'CHN',
            'melbourne': 'AUS',
            'jakarta': 'IDN',
            'tallinn': 'IDN',
            'riga': 'IDN',
        }
    });
});

