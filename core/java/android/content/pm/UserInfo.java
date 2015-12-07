/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;

/**
 * Per-user information.
 * @hide
 */
public class UserInfo implements Parcelable {

    /** 8 bits for user type */
    public static final int FLAG_MASK_USER_TYPE = 0x000000FF;

    /**
     * *************************** NOTE ***************************
     * These flag values CAN NOT CHANGE because they are written
     * directly to storage.
     */

    /**
     * Primary user. Only one user can have this flag set. It identifies the first human user
     * on a device.
     */
    public static final int FLAG_PRIMARY = 0x00000001;

    /**
     * User with administrative privileges. Such a user can create and
     * delete users.
     */
    public static final int FLAG_ADMIN   = 0x00000002;

    /**
     * Indicates a guest user that may be transient.
     */
    public static final int FLAG_GUEST   = 0x00000004;

    /**
     * Indicates the user has restrictions in privileges, in addition to those for normal users.
     * Exact meaning TBD. For instance, maybe they can't install apps or administer WiFi access pts.
     */
    public static final int FLAG_RESTRICTED = 0x00000008;

    /**
     * Indicates that this user has gone through its first-time initialization.
     */
    public static final int FLAG_INITIALIZED = 0x00000010;

    /**
     * Indicates that this user is a profile of another user, for example holding a users
     * corporate data.
     */
    public static final int FLAG_MANAGED_PROFILE = 0x00000020;

    /**
     * Indicates that this user is disabled.
     */
    public static final int FLAG_DISABLED = 0x00000040;

    public static final int FLAG_QUIET_MODE = 0x00000080;

    /**
     * Indicates that this user is ephemeral. I.e. the user will be removed after leaving
     * the foreground.
     */
    public static final int FLAG_EPHEMERAL = 0x00000100;

    public static final int NO_PROFILE_GROUP_ID = UserHandle.USER_NULL;

    public int id;
    public int serialNumber;
    public String name;
    public String iconPath;
    public int flags;
    public long creationTime;
    public long lastLoggedInTime;
    public int profileGroupId;
    public int restrictedProfileParentId;

    /** User is only partially created. */
    public boolean partial;
    public boolean guestToRemove;

    public UserInfo(int id, String name, int flags) {
        this(id, name, null, flags);
    }

    public UserInfo(int id, String name, String iconPath, int flags) {
        this.id = id;
        this.name = name;
        this.flags = flags;
        this.iconPath = iconPath;
        this.profileGroupId = NO_PROFILE_GROUP_ID;
        this.restrictedProfileParentId = NO_PROFILE_GROUP_ID;
    }

    public boolean isPrimary() {
        return (flags & FLAG_PRIMARY) == FLAG_PRIMARY;
    }

    public boolean isAdmin() {
        return (flags & FLAG_ADMIN) == FLAG_ADMIN;
    }

    public boolean isGuest() {
        return (flags & FLAG_GUEST) == FLAG_GUEST;
    }

    public boolean isRestricted() {
        return (flags & FLAG_RESTRICTED) == FLAG_RESTRICTED;
    }

    public boolean isManagedProfile() {
        return (flags & FLAG_MANAGED_PROFILE) == FLAG_MANAGED_PROFILE;
    }

    public boolean isEnabled() {
        return (flags & FLAG_DISABLED) != FLAG_DISABLED;
    }

    public boolean isQuietModeEnabled() {
        return (flags & FLAG_QUIET_MODE) == FLAG_QUIET_MODE;
    }

    public boolean isEphemeral() {
        return (flags & FLAG_EPHEMERAL) == FLAG_EPHEMERAL;
    }

    public boolean isInitialized() {
        return (flags & FLAG_INITIALIZED) == FLAG_INITIALIZED;
    }

    /**
     * Returns true if the user is a split system user.
     * <p>If {@link UserManager#isSplitSystemUser split system user mode} is not enabled,
     * the method always returns false.
     */
    public boolean isSystemOnly() {
        return isSystemOnly(id);
    }

    /**
     * Returns true if the given user is a split system user.
     * <p>If {@link UserManager#isSplitSystemUser split system user mode} is not enabled,
     * the method always returns false.
     */
    public static boolean isSystemOnly(int userId) {
        return userId == UserHandle.USER_SYSTEM && UserManager.isSplitSystemUser();
    }

    /**
     * @return true if this user can be switched to.
     **/
    public boolean supportsSwitchTo() {
        // TODO remove fw.show_hidden_users when we have finished developing managed profiles.
        return !isManagedProfile() || SystemProperties.getBoolean("fw.show_hidden_users", false);
    }

    /**
     * @return true if this user can be switched to by end user through UI.
     */
    public boolean supportsSwitchToByUser() {
        // Hide the system user when it does not represent a human user.
        boolean hideSystemUser = UserManager.isSplitSystemUser();
        return (!hideSystemUser || id != UserHandle.USER_SYSTEM) && supportsSwitchTo();
    }

    /* @hide */
    public boolean canHaveProfile() {
        if (isManagedProfile() || isGuest() || isRestricted()) {
            return false;
        }
        if (UserManager.isSplitSystemUser()) {
            return id != UserHandle.USER_SYSTEM;
        } else {
            return id == UserHandle.USER_SYSTEM;
        }
    }

    public UserInfo() {
    }

    public UserInfo(UserInfo orig) {
        name = orig.name;
        iconPath = orig.iconPath;
        id = orig.id;
        flags = orig.flags;
        serialNumber = orig.serialNumber;
        creationTime = orig.creationTime;
        lastLoggedInTime = orig.lastLoggedInTime;
        partial = orig.partial;
        profileGroupId = orig.profileGroupId;
        guestToRemove = orig.guestToRemove;
    }

    public UserHandle getUserHandle() {
        return new UserHandle(id);
    }

    @Override
    public String toString() {
        return "UserInfo{" + id + ":" + name + ":" + Integer.toHexString(flags) + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(iconPath);
        dest.writeInt(flags);
        dest.writeInt(serialNumber);
        dest.writeLong(creationTime);
        dest.writeLong(lastLoggedInTime);
        dest.writeInt(partial ? 1 : 0);
        dest.writeInt(profileGroupId);
        dest.writeInt(guestToRemove ? 1 : 0);
        dest.writeInt(restrictedProfileParentId);
    }

    public static final Parcelable.Creator<UserInfo> CREATOR
            = new Parcelable.Creator<UserInfo>() {
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    private UserInfo(Parcel source) {
        id = source.readInt();
        name = source.readString();
        iconPath = source.readString();
        flags = source.readInt();
        serialNumber = source.readInt();
        creationTime = source.readLong();
        lastLoggedInTime = source.readLong();
        partial = source.readInt() != 0;
        profileGroupId = source.readInt();
        guestToRemove = source.readInt() != 0;
        restrictedProfileParentId = source.readInt();
    }
}
