import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.util.*

@Serializable
data class Client(
  var name: String,
  @Serializable(UUIDSerializer::class) val uuid: UUID
)

@Serializable
data class Reservation(
  @Serializable(UUIDSerializer::class) var cid: UUID,
  @Serializable(UUIDSerializer::class) var pid: UUID,
  @Serializable(UUIDSerializer::class) var uuid: UUID,
  @Serializable(LocalDateSerializer::class) var from: LocalDate,
  @Serializable(LocalDateSerializer::class) var to: LocalDate,
  val meta: ReservationMeta,
  var prepay: LocalizedPrice,
  var pricePerDay: LocalizedPrice,
  var price: LocalizedPrice,
  var notes: String
)

@Serializable
data class LocalizedPrice(val euro: Double = 0.0, val pln: Double = 0.0) {
  fun getPLN(euroPrice: Double) = (euro * euroPrice) + pln
  fun getEuro(euroPrice: Double) = (pln / euroPrice) + euro
}

@Serializable(ReservationMetaSerializer::class)
data class ReservationMeta(
  var euroPrice: Double,
  var addCleaningCost: Boolean,
  var addCleaningTime: Boolean,
  var keysIncluded: Boolean,
  var ownership: Boolean
)

object ReservationMetaSerializer : KSerializer<ReservationMeta> {
  override val descriptor = PrimitiveSerialDescriptor("ReservationMeta", PrimitiveKind.STRING)
  
  override fun deserialize(decoder: Decoder): ReservationMeta {
    val raw = decoder.decodeString()
    return raw.split('|').let {
      val flags = it[1].toInt()
      ReservationMeta(
        it[0].toDouble(),
        addCleaningCost = flags and 1 == 1,
        addCleaningTime = flags and 2 == 2,
        keysIncluded = flags and 4 == 4,
        ownership = flags and 8 == 8
      )
    }
  }
  
  override fun serialize(encoder: Encoder, value: ReservationMeta) {
    val flags =
      (if (value.addCleaningCost) 1 else 0) + (if (value.addCleaningTime) 2 else 0)
    +(if (value.keysIncluded) 4 else 0) + (if (value.ownership) 8 else 0)
    encoder.encodeString("${value.euroPrice}|$flags")
  }
}

object UUIDSerializer : KSerializer<UUID> {
  override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
  
  override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
  
  override fun serialize(encoder: Encoder, value: UUID) {
    encoder.encodeString(value.toString())
  }
}

object LocalDateSerializer : KSerializer<LocalDate> {
  override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
  
  override fun deserialize(decoder: Decoder): LocalDate =
    decoder.decodeString().let { LocalDate.parse(it) }
  
  override fun serialize(encoder: Encoder, value: LocalDate) {
    encoder.encodeString(value.toString())
  }
}