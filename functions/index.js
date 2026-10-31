const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { HttpsError, onCall } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");

initializeApp();

const db = getFirestore();
const NORMAL_RECEIVER_SHARE = 0.30;
const MAX_QUANTITY = 100;
const GIFTS = {
  gift_single_rose: ["Single Red Rose", "🌹", 1, "micro_gift"],
  gift_lucky_clover: ["Lucky Gold Clover", "🍀", 2, "micro_gift"],
  gift_swirl_lollipop: ["Swirl Candy Lollipop", "🍭", 3, "micro_gift"],
  gift_floating_kiss: ["Floating Kiss & Hearts", "💋", 4, "micro_gift"],
  gift_crystal_lily: ["Crystal Glowing Lily", "🪷", 5, "micro_gift"],
  gift_coffee_cup: ["Hot Heart Coffee", "☕", 6, "micro_gift"],
  gift_perfume_bottle: ["French Amber Perfume", "✨", 7, "micro_gift"],
  gift_rose_bouquet: ["99 Red Roses Bouquet", "💐", 8, "micro_gift"],
  gift_heart_chocolates: ["Heart Velvet Chocolates", "🍫", 10, "micro_gift"],
  gift_teddy_heart: ["Teddy with Ruby Heart", "🧸", 10, "micro_gift"],
  gift_firecracker_bomb: ["Celebration Firecracker", "💥", 12, "firecracker_burst"],
  gift_chocolate_bomb: ["Molten Chocolate Bomb", "💣", 15, "micro_gift"],
  gift_magic_wand: ["Magic Star Wand", "🪄", 18, "micro_gift"],
  gift_golden_mic: ["Vintage Golden Mic", "🎙️", 20, "micro_gift"],
  gift_pearl_necklace: ["Royal Pearl Necklace", "📿", 22, "micro_gift"],
  gift_acoustic_guitar: ["Acoustic Rosewood Guitar", "🎸", 24, "micro_gift"],
  gift_golden_crown: ["Imperial Gold Crown", "👑", 25, "micro_gift"],
  gift_champagne_pop: ["Popping Champagne", "🍾", 28, "micro_gift"],
  gift_diamond_ring: ["Solitaire Diamond Ring", "💍", 30, "micro_gift"],
  gift_gold_watch: ["Swiss Chrono Gold Watch", "⌚", 32, "micro_gift"],
  gift_fireworks_rocket: ["Fireworks Sky Rocket", "🚀", 35, "firecracker_burst"],
  gift_sports_car: ["Neon Exotic Supercar", "🏎️", 40, "car"],
  gift_angel_wings: ["Luminous Angel Wings", "🪽", 42, "micro_gift"],
  gift_crystal_carriage: ["Crystal Royal Carriage", "🪞", 45, "micro_gift"],
  gift_golden_phoenix: ["Mythical Golden Phoenix", "🦅", 50, "micro_gift"],

  lamborghini_mp4: ["Cyber Lamborghini", "🏎️", 30000, "video_mp4"],
  eagle_entry_mp4: ["Fire Phoenix Eagle", "🦅", 35000, "video_mp4"],
  diamond_ring_proposal_mp4: ["Diamond Proposal Ring", "💍", 40000, "video_mp4"],
  king_lion_mp4: ["Blazing Fire Lion", "🦁", 45000, "video_mp4"],
  golden_super_car_mp4: ["Golden Supercar Drift", "🏎️", 50000, "video_mp4"],
  terminator_dragon_mp4: ["Cyber Mecha Dragon", "🐉", 60000, "video_mp4"],
  princess_ruby_necklace_mp4: ["Princess Ruby Necklace", "💎", 65000, "video_mp4"],
  rose_proposal_mp4: ["Rose For Proposal", "🌹", 75000, "video_mp4"],
  treasure_for_you_mp4: ["Treasure For You", "📦", 80000, "video_mp4"],
  king_entry_mp4: ["Imperial King Palace", "👑", 85000, "video_mp4"],
  founded_treasure_mp4: ["Imperial Lost Treasure", "👑", 90000, "video_mp4"],
  heaven_ship_mp4: ["Take You On Heaven Ship", "🛥️", 100000, "video_mp4"],
  royal_jet_mp4: ["Will Take You On Jet", "✈️", 120000, "video_mp4"],
  gift_jeweled_star_mp4: ["Celestial Jeweled Star", "⭐", 30000, "video_mp4"],
  gift_firework_treasure_mp4: ["Firework Treasure", "🎁", 40000, "video_mp4"],
  gift_golden_car_mp4: ["Golden Treasure Car", "🏎️", 50000, "video_mp4"],
  gift_cyber_arena_mp4: ["Cyber Gift Arena", "⚡", 60000, "video_mp4"],
  gift_royal_chest_mp4: ["Royal Treasure Chest", "🧰", 75000, "video_mp4"],
  gift_cosmic_clock_mp4: ["Cosmic Timekeeper", "🌌", 90000, "video_mp4"],
  intimacy_diamond_rain_mp4: ["Diamond Rain of Love", "💎", 30000, "video_mp4"],
  intimacy_soulmate_cloud_mp4: ["Cloudborne Soulmates", "☁️", 35000, "video_mp4"],
  intimacy_sunrise_embrace_mp4: ["Sunrise Embrace", "🌅", 40000, "video_mp4"],
  intimacy_ice_castle_couple_mp4: ["Ice Castle Romance", "❄️", 45000, "video_mp4"],
  intimacy_shadow_proposal_mp4: ["Royal Proposal", "💍", 50000, "video_mp4"],
  intimacy_royal_wedding_mp4: ["Royal Wedding", "💒", 55000, "video_mp4"],
  intimacy_couple_vows_mp4: ["Together Forever", "💞", 60000, "video_mp4"],
  intimacy_waterfall_walk_mp4: ["Waterfall Getaway", "🌿", 65000, "video_mp4"],
};

function readLong(value, field) {
  if (typeof value !== "number" || !Number.isSafeInteger(value) || value < 0) {
    throw new HttpsError("failed-precondition", `Invalid ${field} value.`);
  }
  return value;
}

function levelFromXp(xp) {
  const scale90 = (value) => Math.floor((value * 90) / 100);
  const threshold = (level) => {
    const raw = level <= 1 ? 0
      : level <= 5 ? (level - 1) * 250
        : level <= 10 ? 1000 + (level - 5) * 800
          : level <= 15 ? 5000 + (level - 10) * 1400
            : level <= 20 ? 12000 + (level - 15) * 2000
              : level <= 30 ? 22000 + (level - 20) * 2800
                : level <= 50 ? 50000 + (level - 30) * 7500
                  : level <= 75 ? 200000 + (level - 50) * 10000
                    : level <= 99 ? 450000 + (level - 75) * 45000
                      : 1530000 + (level - 100) * 100000;
    return raw === 0 ? 0 : scale90(raw);
  };
  for (let level = 100; level >= 1; level -= 1) {
    if (xp >= threshold(level)) return level;
  }
  return 1;
}

exports.sendLiveGift = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in to send a gift.");
  }

  const { roomId = "", eventId, giftId, quantity = 1, fromBackpack = false } = request.data || {};
  const senderUid = request.auth.uid;
  const requestedRecipients = request.data?.recipientUids;
  if (typeof eventId !== "string" || !/^[A-Za-z0-9_-]{8,100}$/.test(eventId) ||
      typeof giftId !== "string" || !Object.hasOwn(GIFTS, giftId) ||
      !Number.isInteger(quantity) || quantity < 1 || quantity > MAX_QUANTITY ||
      typeof fromBackpack !== "boolean" ||
      !Array.isArray(requestedRecipients) || requestedRecipients.length < 1 ||
      requestedRecipients.length > 12 ||
      requestedRecipients.some((uid) => typeof uid !== "string" || uid.length < 1 || uid.length > 128 || uid === senderUid) ||
      new Set(requestedRecipients).size !== requestedRecipients.length ||
      (roomId !== "" && (typeof roomId !== "string" || roomId.length > 128)) ||
      (fromBackpack && !giftId.startsWith("gift_"))) {
    throw new HttpsError("invalid-argument", "Gift request is invalid.");
  }

  const [giftName, giftEmoji, unitCost, animationType] = GIFTS[giftId];
  if (fromBackpack && animationType === "video_mp4") {
    throw new HttpsError("invalid-argument", "Video gifts cannot be sent from the backpack.");
  }
  const senderRef = db.collection("users").doc(senderUid);
  const recipientRefs = requestedRecipients.map((uid) => db.collection("users").doc(uid));
  const eventRef = roomId
    ? db.collection("rooms").doc(roomId).collection("gift_events").doc(eventId)
    : senderRef.collection("sent_gifts").doc(eventId);
  const roomRef = roomId ? db.collection("rooms").doc(roomId) : null;
  const timestamp = Date.now();

  const result = await db.runTransaction(async (transaction) => {
    const existingEvent = await transaction.get(eventRef);
    if (existingEvent.exists) {
      const currentSender = await transaction.get(senderRef);
      return {
        duplicate: true,
        event: existingEvent.data(),
        senderDiamonds: currentSender.get("diamonds") ?? null,
      };
    }

    const senderSnapshot = await transaction.get(senderRef);
    if (!senderSnapshot.exists) {
      throw new HttpsError("not-found", "Sender profile is unavailable.");
    }
    const recipientSnapshots = await Promise.all(recipientRefs.map((ref) => transaction.get(ref)));
    if (recipientSnapshots.some((snapshot) => !snapshot.exists)) {
      throw new HttpsError("not-found", "A gift recipient is unavailable.");
    }
    if (roomRef) {
      const roomSnapshot = await transaction.get(roomRef);
      if (!roomSnapshot.exists) {
        throw new HttpsError("not-found", "The room is no longer available.");
      }
    }

    const senderData = senderSnapshot.data();
    const balance = readLong(senderData.diamonds ?? 0, "wallet");
    const backpack = { ...(senderData.backpack || {}) };
    const totalCost = unitCost * quantity * recipientSnapshots.length;
    let remainingDiamonds = balance;
    if (fromBackpack) {
      const count = readLong(backpack[giftId] ?? 0, "backpack");
      const required = quantity * recipientSnapshots.length;
      if (count < required) {
        throw new HttpsError("failed-precondition", "Not enough gifts in the backpack.");
      }
      if (count === required) delete backpack[giftId];
      else backpack[giftId] = count - required;
      transaction.update(senderRef, { backpack });
    } else {
      if (balance < totalCost) {
        throw new HttpsError("failed-precondition", "Not enough diamonds.");
      }
      remainingDiamonds = balance - totalCost;
      const wealthXp = (senderData.wealthXp || 0) + totalCost * 10;
      transaction.update(senderRef, {
        diamonds: remainingDiamonds,
        wealthXp,
        wealthLevel: levelFromXp(wealthXp),
        diamondsSpentCount: FieldValue.increment(totalCost),
      });
    }

    let totalReceiverDiamonds = 0;
    let totalHostEarningsInr = 0;
    const recipients = recipientSnapshots.map((snapshot) => {
      const recipientData = snapshot.data();
      const isMonetizedHost = recipientData.isHostMonetized === true ||
        recipientData.profile_is_host_monetized === true ||
        String(recipientData.role || "").toLowerCase() === "admin";
      const receiverCost = unitCost * quantity;
      const previousRemainder = Number(recipientData.giftDiamondRemainder) || 0;
      const shareWithRemainder = receiverCost * NORMAL_RECEIVER_SHARE + previousRemainder;
      const diamondsReceived = fromBackpack || isMonetizedHost
        ? 0
        : Math.floor(shareWithRemainder);
      const wealthLevel = Number.isInteger(recipientData.wealthLevel) ? recipientData.wealthLevel : 1;
      const hostRate = wealthLevel > 70 ? 8 : wealthLevel >= 50 ? 7 : 5;
      const earningsInr = !fromBackpack && isMonetizedHost
        ? receiverCost * 0.01 * hostRate / 100
        : 0;
      totalReceiverDiamonds += diamondsReceived;
      totalHostEarningsInr += earningsInr;

      const charmXp = (recipientData.charmXp || 0) + (fromBackpack ? 0 : receiverCost * 10);
      const receiverUpdates = {
        charmXp,
        charmLevel: levelFromXp(charmXp),
        ...(!fromBackpack && !isMonetizedHost
          ? { giftDiamondRemainder: shareWithRemainder - diamondsReceived }
          : {}),
        ...(diamondsReceived > 0 ? { diamonds: FieldValue.increment(diamondsReceived) } : {}),
        ...(earningsInr > 0
          ? {
              earningsInr: FieldValue.increment(earningsInr),
              totalEarningsInr: FieldValue.increment(earningsInr),
              hostEarningsInr: FieldValue.increment(earningsInr),
            }
          : {}),
      };
      transaction.update(snapshot.ref, receiverUpdates);

      if (earningsInr > 0) {
        const earningRef = snapshot.ref.collection("earning_transactions").doc(eventId);
        transaction.set(earningRef, {
          id: eventId,
          type: "GIFT_COMMISSION",
          title: `Gift received: ${giftName}`,
          amountInr: earningsInr,
          diamonds: receiverCost,
          senderName: senderData.name || "",
          upiId: "",
          status: "CREDITED",
          timestamp,
        });
      }

      return {
        uid: snapshot.id,
        name: recipientData.name || "",
        diamondsReceived,
        earningsInr,
        isMonetizedHost,
      };
    });

    const event = {
      id: eventId,
      roomId,
      senderId: senderUid,
      senderName: senderData.name || "",
      senderPhoto: senderData.photo || "",
      giftId,
      giftName,
      giftEmoji,
      cost: totalCost,
      animationType,
      timestamp,
      targetUserId: recipients[0].uid,
      targetUserName: recipients.map((recipient) => recipient.name).filter(Boolean).join(", "),
      quantity,
      targetMicSeatIndex: Number.isInteger(request.data?.targetMicSeatIndex)
        ? request.data.targetMicSeatIndex
        : -1,
      fromBackpack,
      receiverUserIds: recipients.map((recipient) => recipient.uid),
      receiverNames: recipients.map((recipient) => recipient.name),
      receiverDiamondPayout: totalReceiverDiamonds,
      receiverEarningsInr: totalHostEarningsInr,
      receiverRate: NORMAL_RECEIVER_SHARE,
    };

    transaction.set(eventRef, event);

    for (const recipient of recipients) {
      const notificationRef = db.collection("users").doc(recipient.uid)
        .collection("notifications").doc(eventId);
      const rootNotificationRef = db.collection("notifications").doc(`${eventId}_${recipient.uid}`);
      const message = recipient.isMonetizedHost
        ? `${senderData.name || "A user"} sent ${giftName}. Host commission: ₹${recipient.earningsInr.toFixed(2)}.`
        : fromBackpack
          ? `${senderData.name || "A user"} sent ${giftName} from a backpack. No diamond payout applies to backpack gifts.`
        : `${senderData.name || "A user"} sent ${giftName}. You received ${recipient.diamondsReceived} Diamonds after the gift split.`;
      const notification = {
        id: eventId,
        targetUserId: recipient.uid,
        senderId: senderUid,
        senderName: "Live Togather",
        senderPhoto: "",
        type: "GIFT_RECEIVED",
        title: `🎁 A gift for you • Live Togather`,
        message,
        timestamp,
        isRead: false,
        roomId,
        giftId,
        giftName,
        giftThumbnailId: giftId,
        diamondsReceived: recipient.diamondsReceived,
        earningsInr: recipient.earningsInr,
      };
      transaction.set(notificationRef, notification);
      transaction.set(rootNotificationRef, { ...notification, userId: recipient.uid });
      transaction.set(db.collection("push_outbox").doc(`${eventId}_${recipient.uid}`), {
        recipientUid: recipient.uid,
        recipientName: recipient.name,
        eventId,
        roomId,
        title: notification.title,
        message,
        giftId,
        createdAt: timestamp,
      });
    }

    return {
      duplicate: false,
      event,
      senderDiamonds: remainingDiamonds,
    };
  });

  return result;
});

exports.deliverGiftPush = onDocumentCreated(
  { document: "push_outbox/{outboxId}", region: "us-central1", retry: true },
  async (event) => {
    const payload = event.data.data();
    const userSnapshot = await db.collection("users").doc(payload.recipientUid).get();
    const token = userSnapshot.get("fcmToken");
    if (typeof token !== "string" || token.length === 0) {
      logger.warn("Gift push skipped because recipient has no registered FCM token.", {
        recipientUid: payload.recipientUid,
        eventId: payload.eventId,
      });
      return;
    }

    try {
      await getMessaging().send({
        token,
        data: {
          type: "GIFT_RECEIVED",
          title: payload.title,
          body: payload.message,
          giftId: payload.giftId,
          roomId: payload.roomId || "",
          eventId: payload.eventId,
        },
        android: {
          priority: "high",
          ttl: 60 * 60 * 1000,
        },
      });
      await event.data.ref.update({ deliveredAt: Date.now() });
    } catch (error) {
      logger.error("Failed to deliver gift notification.", {
        recipientUid: payload.recipientUid,
        eventId: payload.eventId,
        error,
      });
      throw error;
    }
  }
);
