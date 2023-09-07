package bybit.sdk.rest.user

import bybit.sdk.rest.APIResponseV5
import io.ktor.http.*
import kotlinx.serialization.Serializable

suspend fun ByBitUserClient.getKeyInformation(

): KeyInformationResponse =
    byBitRestClient.call({
        path(
            "v5",
            "user",
            "query-api",
        )
    }, HttpMethod.Get, false)


@Serializable
data class KeyInformationResult(
    val id: String,
    val note: String,
    val apiKey: String,
    val readOnly: Int,
    val secret: String,
    val ips: List<String>,
    val userID: String,
    val vipLevel: String,
    val mktMakerLevel: String,
    val deadlineDay: Int,
    val expiredAt: String,
    val createdAt: String,
    val isMaster: Boolean,
    val parentUid: String
    /*

        "id": "13770661",
        "note": "XXXXXX",
        "apiKey": "XXXXXX",
        "readOnly": 0,
        "secret": "",
        "permissions": {
            "ContractTrade": [
                "Order",
                "Position"
            ],
            "Spot": [
                "SpotTrade"
            ],
            "Wallet": [
                "AccountTransfer",
                "SubMemberTransfer"
            ],
            "Options": [
                "OptionsTrade"
            ],
            "Derivatives": [
                "DerivativesTrade"
            ],
            "CopyTrading": [
                "CopyTrading"
            ],
            "BlockTrade": [],
            "Exchange": [
                "ExchangeHistory"
            ],
            "NFT": [
                "NFTQueryProductList"
            ]
        },
        "ips": [
            "*"
        ],
        "type": 1,
        "deadlineDay": 83,
        "expiredAt": "2023-05-15T03:21:05Z",
        "createdAt": "2022-10-16T02:24:40Z",
        "unified": 0,
        "uta": 0,
        "userID": 24600000,
        "inviterID": 0,
        "vipLevel": "No VIP",
        "mktMakerLevel": "0",
        "affiliateID": 0,
        "rsaPublicKey": "",
        "isMaster": false,
        "parentUid": "24617703"

     */
)


@Serializable
data class KeyInformationResponse(
    val result: KeyInformationResult,
) : APIResponseV5()
