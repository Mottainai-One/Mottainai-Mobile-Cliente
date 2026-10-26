package com.mottainai.cliente.repository;

import android.content.Context;

import com.mottainai.cliente.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Native screen definitions and prototype links, bundled for offline use. */
public final class ScreenRepository {
    private static ScreenRepository instance;
    private final JSONObject screens;
    private final Map<String, JSONObject> links = new HashMap<>();

    public static synchronized ScreenRepository get(Context context) {
        if (instance == null) instance = new ScreenRepository(context.getApplicationContext());
        return instance;
    }

    private ScreenRepository(Context context) {
        try {
            screens = new JSONObject(read(context, "design_screens.json"));
            JSONArray navigation = new JSONArray(read(context, "design_navigation.json"));
            for (int i = 0; i < navigation.length(); i++) {
                JSONObject link = navigation.getJSONObject(i);
                links.put(link.getString("id"), link);
            }
        } catch (IOException | JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String read(Context context, String asset) throws IOException {
        try (InputStream stream = context.getAssets().open(asset)) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) != -1) bytes.write(buffer, 0, count);
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    public JSONObject screen(String id) {
        return screens.optJSONObject(id);
    }

    public String canonical(String id) {
        JSONObject screen = screen(id);
        return screen == null ? id : screen.optString("canonical", id);
    }

    public JSONObject link(String id) {
        return links.get(id);
    }

    public JSONObject action(String screenId, String nodeId, String name, String label) {
        JSONObject exact = links.get(nodeId);
        if (exact != null) return exact;
        JSONObject spec = screen(screenId);
        JSONArray actions = spec == null ? null : spec.optJSONArray("actions");
        if (actions == null) return null;
        for (int i = 0; i < actions.length(); i++) {
            JSONObject candidate = actions.optJSONObject(i);
            String candidateName = candidate.optString("name");
            if ((!name.isEmpty() && candidateName.equals(name))
                    || (!label.isEmpty() && candidateName.endsWith(" / " + label))) {
                JSONObject link = links.get(candidate.optString("id"));
                if (link != null) return link;
            }
        }
        return null;
    }

    public boolean available(String id) {
        return screens.has(id);
    }

    // BEGIN SCREEN RESOURCE IDS
    public int layoutFor(String id) {
        switch (canonical(id)) {
            case "2005:3812":
                return R.layout.fragment_home;
            case "2005:3914":
                return R.layout.fragment_catalog;
            case "2005:4018":
                return R.layout.fragment_search;
            case "2005:4104":
                return R.layout.activity_offer_detail;
            case "2005:4221":
                return R.layout.fragment_pickup_availability;
            case "2005:4305":
                return R.layout.fragment_stores;
            case "2005:4391":
                return R.layout.fragment_store_detail;
            case "2005:4483":
                return R.layout.fragment_profile;
            case "2019:4067":
                return R.layout.fragment_search_produce;
            case "2019:4155":
                return R.layout.fragment_search_milk;
            case "2022:4219":
                return R.layout.fragment_account_settings;
            case "2028:4217":
                return R.layout.fragment_change_email;
            case "2028:4279":
                return R.layout.fragment_change_password;
            case "2028:4409":
                return R.layout.dialog_sign_out;
            case "2028:4492":
                return R.layout.fragment_sign_in;
            case "2055:5379":
                return R.layout.activity_assistant;
            case "2055:5504":
                return R.layout.fragment_assistant_draft;
            case "2055:5629":
                return R.layout.fragment_assistant_waiting;
            case "2055:5739":
                return R.layout.fragment_assistant_offer_reply;
            case "2055:5872":
                return R.layout.fragment_assistant_store_reply;
            case "2055:5986":
                return R.layout.fragment_assistant_pickup_reply;
            case "2055:6100":
                return R.layout.fragment_assistant_unavailable;
            case "2055:6213":
                return R.layout.fragment_assistant_send_error;
            case "2093:6331":
                return R.layout.fragment_points;
            case "2093:6456":
                return R.layout.fragment_points_history;
            case "2093:6588":
                return R.layout.fragment_points_earned;
            case "2093:6831":
                return R.layout.fragment_points_help;
            case "2093:7287":
                return R.layout.fragment_points_empty;
            case "2093:7392":
                return R.layout.fragment_points_error;
            case "2143:10074":
                return R.layout.fragment_password_recovery_request;
            case "2143:10166":
                return R.layout.fragment_password_recovery_sent;
            case "2143:10256":
                return R.layout.dialog_region_selection;
            case "2143:10339":
                return R.layout.dialog_location_permission;
            case "2143:10422":
                return R.layout.fragment_location_permission_denied;
            case "2143:10503":
                return R.layout.fragment_location_disabled;
            case "2143:10917":
                return R.layout.dialog_offer_filters;
            case "2143:11079":
                return R.layout.fragment_impact;
            case "2143:9435":
                return R.layout.fragment_registration_personal_data;
            case "2143:9538":
                return R.layout.fragment_registration_region;
            case "2143:9630":
                return R.layout.dialog_registration_confirm_region;
            case "2143:9720":
                return R.layout.fragment_registration_saving;
            case "2143:9806":
                return R.layout.fragment_registration_complete;
            case "2143:9896":
                return R.layout.fragment_registration_name_required;
            case "2143:9986":
                return R.layout.fragment_registration_invalid_postal_code;
            case "2244:11739":
                return R.layout.fragment_app_launch;
            case "2244:11824":
                return R.layout.fragment_signed_in_splash;
            case "2392:19166":
                return R.layout.fragment_session_expired;
            case "2392:19250":
                return R.layout.fragment_registration_unavailable;
            case "2392:19334":
                return R.layout.fragment_registration_completion_personal_data;
            case "2392:19418":
                return R.layout.fragment_registration_completion_address;
            case "2392:19502":
                return R.layout.dialog_registration_review;
            case "2392:19586":
                return R.layout.fragment_registration_completion_saving;
            case "2393:19184":
                return R.layout.fragment_edit_personal_data;
            case "2393:19298":
                return R.layout.fragment_personal_data_saving;
            case "2393:19412":
                return R.layout.fragment_personal_data_saved;
            case "2413:31188":
                return R.layout.fragment_offers_loading;
            case "2413:31330":
                return R.layout.fragment_offers_empty;
            case "2413:31472":
                return R.layout.fragment_offers_error;
            case "2413:31614":
                return R.layout.fragment_offers_pagination_error;
            case "2416:25074":
                return R.layout.fragment_home_loading;
            case "2416:25306":
                return R.layout.fragment_home_balance_unavailable;
            case "2416:25538":
                return R.layout.fragment_home_offers_unavailable;
            case "2416:25770":
                return R.layout.fragment_home_profile_unavailable;
            case "2417:25322":
                return R.layout.fragment_points_balance_loading;
            case "2417:25412":
                return R.layout.fragment_points_balance_error;
            case "2417:25502":
                return R.layout.fragment_points_history_error;
            case "2417:25592":
                return R.layout.fragment_points_month_incomplete;
            case "2418:25646":
                return R.layout.fragment_nearby_stores;
            case "2418:25796":
                return R.layout.fragment_map_unavailable;
            case "2418:25946":
                return R.layout.fragment_store_coordinates_missing;
            case "2418:26096":
                return R.layout.fragment_store_not_found;
            case "2430:27754":
                return R.layout.fragment_registration_address;
            case "2456:28768":
                return R.layout.fragment_email_verify_identity;
            case "2456:28860":
                return R.layout.fragment_email_confirmation_pending;
            case "2456:28952":
                return R.layout.fragment_email_changed;
            case "2456:29044":
                return R.layout.fragment_email_send_error;
            case "2456:29136":
                return R.layout.fragment_password_updated;
            case "2458:28486":
                return R.layout.fragment_email_unconfirmed;
            case "2458:28686":
                return R.layout.fragment_password_saving;
            case "2542:29599":
                return R.layout.fragment_onboarding_offers;
            case "2542:29639":
                return R.layout.fragment_onboarding_pickup;
            case "2542:29679":
                return R.layout.fragment_onboarding_impact;
            case "2557:30312":
                return R.layout.fragment_access_location_permission;
            case "2557:30344":
                return R.layout.fragment_access_location_denied;
            case "2557:30376":
                return R.layout.fragment_access_location_disabled;
            case "2557:30408":
                return R.layout.fragment_access_locating;
            case "2557:30440":
                return R.layout.fragment_access_location_unavailable;
            case "2557:30472":
                return R.layout.fragment_access_session_check;
            case "2557:30504":
                return R.layout.dialog_assistant_history;
            case "2557:30536":
                return R.layout.dialog_assistant_history_empty;
            case "2557:30568":
                return R.layout.dialog_assistant_history_error;
            case "2557:30600":
                return R.layout.fragment_assistant_conversation_closed;
            case "2557:30632":
                return R.layout.fragment_assistant_delivery_unconfirmed;
            case "2557:30760":
                return R.layout.fragment_nearby_alerts;
            case "2557:30792":
                return R.layout.fragment_notification_permission;
            case "2557:30824":
                return R.layout.fragment_notifications_disabled;
            case "2557:30856":
                return R.layout.fragment_background_notifications;
            case "2557:30888":
                return R.layout.fragment_notification_example;
            case "2557:30920":
                return R.layout.fragment_notification_offer_ended;
            case "2557:30952":
                return R.layout.dialog_points_refund;
            case "2557:30984":
                return R.layout.fragment_loyalty_status_unavailable;
            case "2557:31016":
                return R.layout.dialog_loyalty_share;
            case "2557:31048":
                return R.layout.fragment_offer_image_unavailable;
            case "2565:30879":
                return R.layout.dialog_loyalty_levels;
            case "2565:30911":
                return R.layout.fragment_device_settings;
            case "2571:30930":
                return R.layout.dialog_profile_photo_picker;
            case "2571:30962":
                return R.layout.dialog_profile_photo_review;
            case "2571:30994":
                return R.layout.dialog_profile_photo_saving;
            case "2650:73471":
                return R.layout.fragment_communication_preferences;
            case "2650:73528":
                return R.layout.fragment_communication_saving;
            case "2650:73585":
                return R.layout.fragment_communication_saved;
            case "2650:73642":
                return R.layout.fragment_communication_error;
            case "2680:78485":
                return R.layout.dialog_camera_permission;
            case "2680:78487":
                return R.layout.fragment_camera_capture;
            case "2680:78489":
                return R.layout.dialog_camera_denied;
            default:
                throw new IllegalArgumentException("Unknown screen: " + id);
        }
    }
    // END SCREEN RESOURCE IDS
}
